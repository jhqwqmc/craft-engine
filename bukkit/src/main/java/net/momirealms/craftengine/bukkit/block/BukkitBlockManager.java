package net.momirealms.craftengine.bukkit.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.momirealms.craftengine.bukkit.block.behavior.UnsafeCompositeBlockBehavior;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.injector.BlockGenerator;
import net.momirealms.craftengine.bukkit.plugin.reflection.bukkit.CraftBukkitReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.*;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.core.block.*;
import net.momirealms.craftengine.core.block.behavior.AbstractBlockBehavior;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.behavior.EmptyBlockBehavior;
import net.momirealms.craftengine.core.block.parser.BlockStateParser;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.sound.SoundSet;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.chunk.PalettedContainer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class BukkitBlockManager extends AbstractBlockManager {
    public static final Set<Object> CLIENT_SIDE_NOTE_BLOCKS = new HashSet<>(2048, 0.6f);
    private static final Object ALWAYS_FALSE = FastNMS.INSTANCE.method$StatePredicate$always(false);
    private static final Object ALWAYS_TRUE = FastNMS.INSTANCE.method$StatePredicate$always(true);
    private static BukkitBlockManager instance;
    private final BukkitCraftEngine plugin;
    // 事件监听器
    private final BlockEventListener blockEventListener;
    // 用于缓存string形式的方块状态到原版方块状态
    private final Map<String, BlockStateWrapper> blockStateCache = new HashMap<>(1024);
    // 用于临时存储可燃烧自定义方块的列表
    private final List<DelegatingBlock> burnableBlocks = new ArrayList<>();
    // 可燃烧的方块
    private Map<Object, Integer> igniteOdds;
    private Map<Object, Integer> burnOdds;
    // 自定义客户端侧原版方块标签
    private Map<Integer, List<String>> clientBoundTags = Map.of();
    private Map<Integer, List<String>> previousClientBoundTags = Map.of();
    // 缓存的原版方块tag包
    private Object cachedUpdateTagsPacket;
    // 被移除声音的原版方块
    private Set<Object> missingPlaceSounds = Set.of();
    private Set<Object> missingBreakSounds = Set.of();
    private Set<Object> missingHitSounds = Set.of();
    private Set<Object> missingStepSounds = Set.of();
    private Set<Key> missingInteractSoundBlocks = Set.of();

    public BukkitBlockManager(BukkitCraftEngine plugin) {
        super(plugin, RegistryUtils.currentBlockRegistrySize(), Config.serverSideBlocks());
        this.plugin = plugin;
        this.blockEventListener = new BlockEventListener(plugin, this);
        this.registerServerSideCustomBlocks(Config.serverSideBlocks());
        EmptyBlock.initialize();
        instance = this;
    }

    @Override
    public void init() {
        this.initMirrorRegistry();
        this.initFireBlock();
        this.deceiveBukkitRegistry();
        this.markVanillaNoteBlocks();
        Arrays.fill(this.immutableBlockStates, EmptyBlock.INSTANCE.defaultState());
        this.plugin.networkManager().registerBlockStatePacketListeners(this.blockStateMappings); // 一定要预先初始化一次，预防id超出上限
    }

    public static BukkitBlockManager instance() {
        return instance;
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this.blockEventListener, this.plugin.javaPlugin());
    }

    @Override
    public void unload() {
        super.unload();
        this.previousClientBoundTags = this.clientBoundTags;
        this.clientBoundTags = new HashMap<>();
        for (DelegatingBlock block : this.burnableBlocks) {
            this.igniteOdds.remove(block);
            this.burnOdds.remove(block);
        }
        this.burnableBlocks.clear();
        if (EmptyBlock.STATE != null)
            Arrays.fill(this.immutableBlockStates, EmptyBlock.STATE);
        for (DelegatingBlock block : this.customBlocks) {
            block.behaviorDelegate().bindValue(EmptyBlockBehavior.INSTANCE);
            block.shapeDelegate().bindValue(BukkitBlockShape.STONE);
            DelegatingBlockState state = (DelegatingBlockState) FastNMS.INSTANCE.method$Block$defaultState(block);
            state.setBlockState(null);
        }
    }

    @Override
    public void disable() {
        this.unload();
        HandlerList.unregisterAll(this.blockEventListener);
    }

    @Override
    public void delayedLoad() {
        this.plugin.networkManager().registerBlockStatePacketListeners(this.blockStateMappings); // 重置方块映射表
        super.delayedLoad();
    }

    @Override
    public BlockBehavior createBlockBehavior(CustomBlock customBlock, List<Map<String, Object>> behaviorConfig) {
        if (behaviorConfig == null || behaviorConfig.isEmpty()) {
            return new EmptyBlockBehavior();
        } else if (behaviorConfig.size() == 1) {
            return BlockBehaviors.fromMap(customBlock, behaviorConfig.getFirst());
        } else {
            List<AbstractBlockBehavior> behaviors = new ArrayList<>();
            for (Map<String, Object> config : behaviorConfig) {
                behaviors.add((AbstractBlockBehavior) BlockBehaviors.fromMap(customBlock, config));
            }
            return new UnsafeCompositeBlockBehavior(customBlock, behaviors);
        }
    }

    @Override
    protected void resendTags() {
        // if there's no change
        if (this.clientBoundTags.equals(this.previousClientBoundTags)) return;
        List<TagUtils.TagEntry> list = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : this.clientBoundTags.entrySet()) {
            list.add(new TagUtils.TagEntry(entry.getKey(), entry.getValue()));
        }
        Object packet = TagUtils.createUpdateTagsPacket(Map.of(MRegistries.BLOCK, list));
        for (BukkitServerPlayer player : this.plugin.networkManager().onlineUsers()) {
            player.sendPacket(packet, false);
        }
        // 如果空，那么新来的玩家就没必要收到更新包了
        if (list.isEmpty()) {
            this.cachedUpdateTagsPacket = null;
        } else {
            this.cachedUpdateTagsPacket = packet;
        }
    }

    @Nullable
    @Override
    public BlockStateWrapper createBlockState(String blockState) {
        ImmutableBlockState state = BlockStateParser.deserialize(blockState);
        if (state != null) {
            return state.customBlockState();
        }
        return createVanillaBlockState(blockState);
    }

    @Override
    public BlockStateWrapper createVanillaBlockState(String blockState) {
        return this.blockStateCache.computeIfAbsent(blockState, k -> {
            Object state = parseBlockState(k);
            if (state == null) return null;
            return BlockStateUtils.toBlockStateWrapper(state);
        });
    }

    @Nullable
    private Object parseBlockState(String state) {
        try {
            Object registryOrLookUp = MBuiltInRegistries.BLOCK;
            if (CoreReflections.method$Registry$asLookup != null) {
                registryOrLookUp = CoreReflections.method$Registry$asLookup.invoke(registryOrLookUp);
            }
            Object result = CoreReflections.method$BlockStateParser$parseForBlock.invoke(null, registryOrLookUp, state, false);
            return CoreReflections.method$BlockStateParser$BlockResult$blockState.invoke(result);
        } catch (Exception e) {
            Debugger.BLOCK.warn(() -> "Failed to create block state: " + state, e);
            return null;
        }
    }

    @Nullable
    public Object getMinecraftBlockHolder(int stateId) {
        return this.customBlockHolders[stateId - BlockStateUtils.vanillaBlockStateCount()];
    }

    @Override
    public Key getBlockOwnerId(BlockStateWrapper state) {
        return BlockStateUtils.getBlockOwnerIdFromState(state.literalObject());
    }

    @Override
    protected Key getBlockOwnerId(int id) {
        return BlockStateUtils.getBlockOwnerIdFromState(BlockStateUtils.idToBlockState(id));
    }

    @SuppressWarnings("unchecked")
    private void initFireBlock() {
        try {
            this.igniteOdds = (Map<Object, Integer>) CoreReflections.field$FireBlock$igniteOdds.get(MBlocks.FIRE);
            this.burnOdds = (Map<Object, Integer>) CoreReflections.field$FireBlock$burnOdds.get(MBlocks.FIRE);
        } catch (IllegalAccessException e) {
            this.plugin.logger().warn("Failed to get ignite odds", e);
        }
    }

    @Override
    protected void applyPlatformSettings(ImmutableBlockState state) {
        DelegatingBlockState nmsState = (DelegatingBlockState) state.customBlockState().literalObject();
        nmsState.setBlockState(state);
        Object nmsVisualState = state.vanillaBlockState().literalObject();

        BlockSettings settings = state.settings();
        try {
            CoreReflections.field$BlockStateBase$lightEmission.set(nmsState, settings.luminance());
            CoreReflections.field$BlockStateBase$burnable.set(nmsState, settings.burnable());
            CoreReflections.field$BlockStateBase$hardness.set(nmsState, settings.hardness());
            CoreReflections.field$BlockStateBase$replaceable.set(nmsState, settings.replaceable());
            Object mcMapColor = CoreReflections.method$MapColor$byId.invoke(null, settings.mapColor().id);
            CoreReflections.field$BlockStateBase$mapColor.set(nmsState, mcMapColor);
            CoreReflections.field$BlockStateBase$instrument.set(nmsState, CoreReflections.instance$NoteBlockInstrument$values[settings.instrument().ordinal()]);
            CoreReflections.field$BlockStateBase$pushReaction.set(nmsState, CoreReflections.instance$PushReaction$values[settings.pushReaction().ordinal()]);
            boolean canOcclude = settings.canOcclude() == Tristate.UNDEFINED ? BlockStateUtils.isOcclude(nmsVisualState) : settings.canOcclude().asBoolean();
            CoreReflections.field$BlockStateBase$canOcclude.set(nmsState, canOcclude);
            boolean useShapeForLightOcclusion = settings.useShapeForLightOcclusion() == Tristate.UNDEFINED ? CoreReflections.field$BlockStateBase$useShapeForLightOcclusion.getBoolean(nmsVisualState) : settings.useShapeForLightOcclusion().asBoolean();
            CoreReflections.field$BlockStateBase$useShapeForLightOcclusion.set(nmsState, useShapeForLightOcclusion);
            CoreReflections.field$BlockStateBase$isRedstoneConductor.set(nmsState, settings.isRedstoneConductor().asBoolean() ? ALWAYS_TRUE : ALWAYS_FALSE);
            CoreReflections.field$BlockStateBase$isSuffocating.set(nmsState, settings.isSuffocating().asBoolean() ? ALWAYS_TRUE : ALWAYS_FALSE);
            CoreReflections.field$BlockStateBase$isViewBlocking.set(nmsState, settings.isViewBlocking() == Tristate.UNDEFINED ? settings.isSuffocating().asBoolean() ? ALWAYS_TRUE : ALWAYS_FALSE : (settings.isViewBlocking().asBoolean() ? ALWAYS_TRUE : ALWAYS_FALSE));

            DelegatingBlock nmsBlock = (DelegatingBlock) BlockStateUtils.getBlockOwner(nmsState);
            ObjectHolder<BlockShape> shapeHolder = nmsBlock.shapeDelegate();
            shapeHolder.bindValue(new BukkitBlockShape(nmsVisualState, Optional.ofNullable(state.settings().supportShapeBlockState()).map(it -> Objects.requireNonNull(createVanillaBlockState(it), "Illegal block state: " + it).literalObject()).orElse(null)));
            ObjectHolder<BlockBehavior> behaviorHolder = nmsBlock.behaviorDelegate();
            behaviorHolder.bindValue(state.behavior());

            CoreReflections.field$BlockBehaviour$explosionResistance.set(nmsBlock, settings.resistance());
            CoreReflections.field$BlockBehaviour$friction.set(nmsBlock, settings.friction());
            CoreReflections.field$BlockBehaviour$speedFactor.set(nmsBlock, settings.speedFactor());
            CoreReflections.field$BlockBehaviour$jumpFactor.set(nmsBlock, settings.jumpFactor());
            CoreReflections.field$BlockBehaviour$soundType.set(nmsBlock, SoundUtils.toSoundType(settings.sounds()));

            CoreReflections.method$BlockStateBase$initCache.invoke(nmsState);
            boolean isConditionallyFullOpaque = canOcclude & useShapeForLightOcclusion;
            if (!VersionHelper.isOrAbove1_21_2()) {
                CoreReflections.field$BlockStateBase$isConditionallyFullOpaque.set(nmsState, isConditionallyFullOpaque);
            }

            if (VersionHelper.isOrAbove1_21_2()) {
                int blockLight = settings.blockLight() != -1 ? settings.blockLight() : CoreReflections.field$BlockStateBase$lightBlock.getInt(nmsVisualState);
                CoreReflections.field$BlockStateBase$lightBlock.set(nmsState, blockLight);
                boolean propagatesSkylightDown = settings.propagatesSkylightDown() == Tristate.UNDEFINED ? CoreReflections.field$BlockStateBase$propagatesSkylightDown.getBoolean(nmsVisualState) : settings.propagatesSkylightDown().asBoolean();
                CoreReflections.field$BlockStateBase$propagatesSkylightDown.set(nmsState, propagatesSkylightDown);
            } else {
                Object cache = CoreReflections.field$BlockStateBase$cache.get(nmsState);
                int blockLight = settings.blockLight() != -1 ? settings.blockLight() : CoreReflections.field$BlockStateBase$Cache$lightBlock.getInt(CoreReflections.field$BlockStateBase$cache.get(nmsVisualState));
                CoreReflections.field$BlockStateBase$Cache$lightBlock.set(cache, blockLight);
                boolean propagatesSkylightDown = settings.propagatesSkylightDown() == Tristate.UNDEFINED ? CoreReflections.field$BlockStateBase$Cache$propagatesSkylightDown.getBoolean(CoreReflections.field$BlockStateBase$cache.get(nmsVisualState)) : settings.propagatesSkylightDown().asBoolean();
                CoreReflections.field$BlockStateBase$Cache$propagatesSkylightDown.set(cache, propagatesSkylightDown);
                if (!isConditionallyFullOpaque) {
                    CoreReflections.field$BlockStateBase$opacityIfCached.set(nmsState, blockLight);
                }
            }

            CoreReflections.field$BlockStateBase$fluidState.set(nmsState, settings.fluidState() ? MFluids.WATER$defaultState : MFluids.EMPTY$defaultState);
            CoreReflections.field$BlockStateBase$isRandomlyTicking.set(nmsState, settings.isRandomlyTicking());
            Object holder = BukkitCraftEngine.instance().blockManager().getMinecraftBlockHolder(state.customBlockState().registryId());
            Set<Object> tags = new HashSet<>();
            for (Key tag : settings.tags()) {
                tags.add(CoreReflections.method$TagKey$create.invoke(null, MRegistries.BLOCK, KeyUtils.toResourceLocation(tag)));
            }
            CoreReflections.field$Holder$Reference$tags.set(holder, tags);
            if (settings.burnable()) {
                this.igniteOdds.put(nmsBlock, settings.burnChance());
                this.burnOdds.put(nmsBlock, settings.fireSpreadChance());
                this.burnableBlocks.add(nmsBlock);
            }
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to apply platform block settings for block state " + state, e);
        }
    }

    private BlockSounds toBlockSounds(Object soundType) throws ReflectiveOperationException {
        return new BlockSounds(
                toSoundData(CoreReflections.field$SoundType$breakSound.get(soundType), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8),
                toSoundData(CoreReflections.field$SoundType$stepSound.get(soundType), SoundData.SoundValue.FIXED_0_15, SoundData.SoundValue.FIXED_1),
                toSoundData(CoreReflections.field$SoundType$placeSound.get(soundType), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8),
                toSoundData(CoreReflections.field$SoundType$hitSound.get(soundType), SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.FIXED_0_5),
                toSoundData(CoreReflections.field$SoundType$fallSound.get(soundType), SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.FIXED_0_75)
        );
    }

    private SoundData toSoundData(Object soundEvent, SoundData.SoundValue volume, SoundData.SoundValue pitch) {
        Key soundId = KeyUtils.resourceLocationToKey(FastNMS.INSTANCE.field$SoundEvent$location(soundEvent));
        return new SoundData(soundId, volume, pitch);
    }

    private void initMirrorRegistry() {
        int size = RegistryUtils.currentBlockRegistrySize();
        BlockStateWrapper[] states = new BlockStateWrapper[size];
        for (int i = 0; i < this.vanillaBlockStateCount; i++) {
            states[i] = new BukkitVanillaBlockStateWrapper(BlockStateUtils.idToBlockState(i), i);
        }
        for (int i = this.vanillaBlockStateCount; i < size; i++) {
            states[i] = new BukkitCustomBlockStateWrapper(BlockStateUtils.idToBlockState(i), i);
        }
        BlockRegistryMirror.init(states, states[BlockStateUtils.blockStateToId(MBlocks.STONE$defaultState)]);
    }

    // 注册服务端侧的真实方块
    private void registerServerSideCustomBlocks(int count) {
        // 这个会影响全局调色盘
        if (MiscUtils.ceilLog2(this.vanillaBlockStateCount + count) == MiscUtils.ceilLog2(this.vanillaBlockStateCount)) {
            PalettedContainer.NEED_DOWNGRADE = false;
        }
        try {
            unfreezeRegistry();
            for (int i = 0; i < count; i++) {
                Key customBlockId = BlockManager.createCustomBlockKey(i);
                DelegatingBlock customBlock;
                try {
                    customBlock = BlockGenerator.generateBlock(customBlockId);
                } catch (Throwable t) {
                    CraftEngine.instance().logger().warn("Failed to generate custom block " + customBlockId, t);
                    break;
                }
                this.customBlocks[i] = customBlock;
                try {
                    Object resourceLocation = KeyUtils.toResourceLocation(customBlockId);
                    Object blockHolder = CoreReflections.method$Registry$registerForHolder.invoke(null, MBuiltInRegistries.BLOCK, resourceLocation, customBlock);
                    this.customBlockHolders[i] = blockHolder;
                    CoreReflections.method$Holder$Reference$bindValue.invoke(blockHolder, customBlock);
                    CoreReflections.field$Holder$Reference$tags.set(blockHolder, Set.of());
                    DelegatingBlockState newBlockState = (DelegatingBlockState) FastNMS.INSTANCE.method$Block$defaultState(customBlock);
                    this.customBlockStates[i] = newBlockState;
                    CoreReflections.method$IdMapper$add.invoke(CoreReflections.instance$Block$BLOCK_STATE_REGISTRY, newBlockState);
                } catch (ReflectiveOperationException e) {
                    CraftEngine.instance().logger().warn("Failed to register custom block " + customBlockId, e);
                }
            }
        } finally {
            freezeRegistry();
        }
    }

    public Object cachedUpdateTagsPacket() {
        return this.cachedUpdateTagsPacket;
    }

    private void markVanillaNoteBlocks() {
        try {
            Object block = FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.BLOCK, KeyUtils.toResourceLocation(BlockKeys.NOTE_BLOCK));
            Object stateDefinition = CoreReflections.field$Block$StateDefinition.get(block);
            @SuppressWarnings("unchecked")
            ImmutableList<Object> states = (ImmutableList<Object>) CoreReflections.field$StateDefinition$states.get(stateDefinition);
            CLIENT_SIDE_NOTE_BLOCKS.addAll(states);
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to init vanilla note block", e);
        }
    }

    @Override
    protected void setVanillaBlockTags(Key id, List<String> tags) {
        Object block = FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.BLOCK, KeyUtils.toResourceLocation(id));
        this.clientBoundTags.put(FastNMS.INSTANCE.method$IdMap$getId(MBuiltInRegistries.BLOCK, block).orElseThrow(() -> new IllegalStateException("Block " + id + " not found")), tags);
    }

    public boolean isPlaceSoundMissing(Object sound) {
        return this.missingPlaceSounds.contains(sound);
    }

    public boolean isBreakSoundMissing(Object sound) {
        return this.missingBreakSounds.contains(sound);
    }

    public boolean isHitSoundMissing(Object sound) {
        return this.missingHitSounds.contains(sound);
    }

    public boolean isStepSoundMissing(Object sound) {
        return this.missingStepSounds.contains(sound);
    }

    public boolean isInteractSoundMissing(Key blockType) {
        return this.missingInteractSoundBlocks.contains(blockType);
    }

    private void unfreezeRegistry() {
        try {
            CoreReflections.field$MappedRegistry$frozen.set(MBuiltInRegistries.BLOCK, false);
            CoreReflections.field$MappedRegistry$unregisteredIntrusiveHolders.set(MBuiltInRegistries.BLOCK, new IdentityHashMap<>());
        } catch (IllegalAccessException e) {
            this.plugin.logger().warn("Failed to unfreeze block registry", e);
        }
    }

    private void freezeRegistry() {
        try {
            CoreReflections.field$MappedRegistry$frozen.set(MBuiltInRegistries.BLOCK, true);
        } catch (IllegalAccessException e) {
            this.plugin.logger().warn("Failed to freeze block registry", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void deceiveBukkitRegistry() {
        try {
            Material material;
            try {
                material = Material.valueOf(Config.deceiveBukkitMaterial().value().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                this.plugin.logger().warn(Config.deceiveBukkitMaterial() + " is not a valid material", e);
                material = Material.STONE;
            }
            if (!material.isBlock()) {
                this.plugin.logger().warn(Config.deceiveBukkitMaterial() + " is not a valid bukkit block material");
                material = Material.STONE;
            }
            Map<Object, Material> magicMap = (Map<Object, Material>) CraftBukkitReflections.field$CraftMagicNumbers$BLOCK_MATERIAL.get(null);
            for (DelegatingBlock customBlock : this.customBlocks) {
                magicMap.put(customBlock, material);
            }
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to deceive bukkit magic blocks", e);
        }
    }

    @Override
    protected boolean isVanillaBlock(Key id) {
        if (!id.namespace().equals("minecraft"))
            return false;
        if (id.value().equals("air"))
            return true;
        return FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.BLOCK, KeyUtils.toResourceLocation(id)) != MBlocks.AIR;
    }

    public boolean isBurnable(Object blockState) {
        Object blockOwner = BlockStateUtils.getBlockOwner(blockState);
        return this.igniteOdds.getOrDefault(blockOwner, 0) > 0;
    }

    @Override
    public int vanillaBlockStateCount() {
        return this.vanillaBlockStateCount;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected void processSounds() {
        Set<Object> affectedBlockSoundTypes = new HashSet<>();
        for (BlockStateWrapper vanillaBlockState : super.tempVisualBlockStatesInUse) {
            affectedBlockSoundTypes.add(FastNMS.INSTANCE.method$BlockBehaviour$BlockStateBase$getSoundType(vanillaBlockState.literalObject()));
        }

        Set<Object> placeSounds = new HashSet<>();
        Set<Object> breakSounds = new HashSet<>();
        Set<Object> stepSounds = new HashSet<>();
        Set<Object> hitSounds = new HashSet<>();

        for (Object soundType : affectedBlockSoundTypes) {
            placeSounds.add(FastNMS.INSTANCE.field$SoundEvent$location(FastNMS.INSTANCE.field$SoundType$placeSound(soundType)));
            breakSounds.add(FastNMS.INSTANCE.field$SoundEvent$location(FastNMS.INSTANCE.field$SoundType$breakSound(soundType)));
            stepSounds.add(FastNMS.INSTANCE.field$SoundEvent$location(FastNMS.INSTANCE.field$SoundType$stepSound(soundType)));
            hitSounds.add(FastNMS.INSTANCE.field$SoundEvent$location(FastNMS.INSTANCE.field$SoundType$hitSound(soundType)));
        }

        ImmutableMap.Builder<Key, Key> soundReplacementBuilder = ImmutableMap.builder();
        for (Object soundId : placeSounds) {
            Key previousId = KeyUtils.resourceLocationToKey(soundId);
            soundReplacementBuilder.put(previousId, Key.of(previousId.namespace(), "replaced." + previousId.value()));
        }
        for (Object soundId : breakSounds) {
            Key previousId = KeyUtils.resourceLocationToKey(soundId);
            soundReplacementBuilder.put(previousId, Key.of(previousId.namespace(), "replaced." + previousId.value()));
        }
        for (Object soundId : stepSounds) {
            Key previousId = KeyUtils.resourceLocationToKey(soundId);
            soundReplacementBuilder.put(previousId, Key.of(previousId.namespace(), "replaced." + previousId.value()));
        }
        for (Object soundId : hitSounds) {
            Key previousId = KeyUtils.resourceLocationToKey(soundId);
            soundReplacementBuilder.put(previousId, Key.of(previousId.namespace(), "replaced." + previousId.value()));
        }

        this.missingPlaceSounds = placeSounds;
        this.missingBreakSounds = breakSounds;
        this.missingHitSounds = hitSounds;
        this.missingStepSounds = stepSounds;

        Set<Key> missingInteractSoundBlocks = new HashSet<>();

        for (SoundSet soundSet : SoundSet.getAllSoundSets()) {
            for (Key block : soundSet.blocks()) {
                if (super.tempVisualBlocksInUse.contains(block)) {
                    Key openSound = soundSet.openSound();
                    soundReplacementBuilder.put(openSound, Key.of(openSound.namespace(), "replaced." + openSound.value()));
                    Key closeSound = soundSet.closeSound();
                    soundReplacementBuilder.put(closeSound, Key.of(closeSound.namespace(), "replaced." + closeSound.value()));
                    missingInteractSoundBlocks.addAll(soundSet.blocks());
                    break;
                }
            }
        }

        this.missingInteractSoundBlocks = missingInteractSoundBlocks;
        this.soundReplacements = soundReplacementBuilder.buildKeepingLast();
    }

    @Override
    protected CustomBlock createCustomBlock(@NotNull Holder.Reference<CustomBlock> holder, 
                                            @NotNull BlockStateVariantProvider variantProvider,
                                            @NotNull Map<EventTrigger, List<Function<PlayerOptionalContext>>> events,
                                            @Nullable LootTable<?> lootTable) {
        return new BukkitCustomBlock(holder, variantProvider, events, lootTable);
    }
}
