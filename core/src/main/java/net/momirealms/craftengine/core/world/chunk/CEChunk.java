package net.momirealms.craftengine.core.world.chunk;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.render.ConstantBlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.tick.*;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.entityculling.CullingData;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.world.*;
import net.momirealms.craftengine.core.world.chunk.client.VirtualCullableObject;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultBlockEntityRendererSerializer;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultBlockEntitySerializer;
import net.momirealms.sparrow.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CEChunk {
    public final CEWorld world;
    public final ChunkPos chunkPos;
    public final CESection[] sections;
    public final WorldHeight worldHeightAccessor;
    public final Map<BlockPos, BlockEntity> blockEntities;  // 从区域线程上访问，安全
    public final Map<BlockPos, ReplaceableTickingBlockEntity> tickingSyncBlockEntitiesByPos; // 从区域线程上访问，安全
    public final Map<BlockPos, ReplaceableTickingBlockEntity> tickingAsyncBlockEntitiesByPos; // 从区域线程上访问，安全
    public final Map<BlockPos, ConstantBlockEntityRenderer> constantBlockEntityRenderers; // 会从区域线程上读写，netty线程上读取
    public final Map<BlockPos, DynamicBlockEntityRenderer> dynamicBlockEntityRenderers; // 会从区域线程上读写，netty线程上读取
    private final ReentrantReadWriteLock renderLock = new ReentrantReadWriteLock();
    private volatile boolean dirty;
    private volatile boolean loaded;
    private volatile boolean activated;

    public CEChunk(CEWorld world, ChunkPos chunkPos) {
        this.world = world;
        this.chunkPos = chunkPos;
        this.worldHeightAccessor = world.worldHeight();
        this.sections = new CESection[this.worldHeightAccessor.getSectionsCount()];
        this.blockEntities = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.constantBlockEntityRenderers = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.dynamicBlockEntityRenderers = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.tickingSyncBlockEntitiesByPos = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.tickingAsyncBlockEntitiesByPos = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.fillEmptySection();
    }

    public CEChunk(CEWorld world, ChunkPos chunkPos, CESection[] sections, @Nullable ListTag blockEntitiesTag, @Nullable ListTag itemDisplayBlockRenders) {
        this.world = world;
        this.chunkPos = chunkPos;
        this.worldHeightAccessor = world.worldHeight();
        this.dynamicBlockEntityRenderers = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.tickingSyncBlockEntitiesByPos = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.tickingAsyncBlockEntitiesByPos = new Object2ObjectOpenHashMap<>(10, 0.5f);
        int sectionCount = this.worldHeightAccessor.getSectionsCount();
        this.sections = new CESection[sectionCount];
        if (sections != null) {
            for (CESection section : sections) {
                if (section != null) {
                    int index = sectionIndex(section.sectionY());
                    this.sections[index] = section;
                }
            }
        }
        this.fillEmptySection();
        if (blockEntitiesTag != null) {
            this.blockEntities = new Object2ObjectOpenHashMap<>(Math.max(blockEntitiesTag.size(), 10), 0.5f);
            List<BlockEntity> blockEntities = DefaultBlockEntitySerializer.deserialize(this, blockEntitiesTag);
            for (BlockEntity blockEntity : blockEntities) {
                this.setBlockEntity(blockEntity);
            }
        } else {
            this.blockEntities = new Object2ObjectOpenHashMap<>(10, 0.5f);
        }
        if (itemDisplayBlockRenders != null) {
            this.constantBlockEntityRenderers = new Object2ObjectOpenHashMap<>(Math.max(itemDisplayBlockRenders.size(), 10), 0.5f);
            List<BlockPos> blockEntityRendererPoses = DefaultBlockEntityRendererSerializer.deserialize(this.chunkPos, itemDisplayBlockRenders);
            for (BlockPos pos : blockEntityRendererPoses) {
                this.addConstantBlockEntityRenderer(pos);
            }
        } else {
            this.constantBlockEntityRenderers = new Object2ObjectOpenHashMap<>(10, 0.5f);
        }
    }

    public void spawnBlockEntities(Player player) {
        try {
            this.renderLock.readLock().lock();
            if (Config.enableEntityCulling()) {
                player.addTrackedBlockEntities(this.constantBlockEntityRenderers);
            } else {
                for (ConstantBlockEntityRenderer renderer : this.constantBlockEntityRenderers.values()) {
                    renderer.show(player);
                }
            }
            for (DynamicBlockEntityRenderer renderer : this.dynamicBlockEntityRenderers.values()) {
                renderer.show(player);
            }
        } finally {
            this.renderLock.readLock().unlock();
        }
    }

    public void despawnBlockEntities(Player player) {
        try {
            this.renderLock.readLock().lock();
            if (Config.enableEntityCulling()) {
                player.removeTrackedBlockEntities(this.constantBlockEntityRenderers.keySet());
            } else {
                for (ConstantBlockEntityRenderer renderer : this.constantBlockEntityRenderers.values()) {
                    renderer.hide(player);
                }
            }
            for (DynamicBlockEntityRenderer renderer : this.dynamicBlockEntityRenderers.values()) {
                renderer.hide(player);
            }
        } finally {
            this.renderLock.readLock().unlock();
        }
    }

    public ConstantBlockEntityRenderer addConstantBlockEntityRenderer(BlockPos pos) {
        return this.addConstantBlockEntityRenderer(pos, this.getBlockState(pos), null);
    }

    public ConstantBlockEntityRenderer addConstantBlockEntityRenderer(BlockPos pos, ImmutableBlockState state) {
        return this.addConstantBlockEntityRenderer(pos, state, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ConstantBlockEntityRenderer addConstantBlockEntityRenderer(BlockPos pos, ImmutableBlockState state, @Nullable ConstantBlockEntityRenderer previous) {
        BlockEntityElementConfig<? extends BlockEntityElement>[] renderers = state.constantRenderers();
        if (renderers != null && renderers.length > 0) {
            BlockEntityElement[] elements = new BlockEntityElement[renderers.length];
            ConstantBlockEntityRenderer renderer = new ConstantBlockEntityRenderer(
                    elements,
                    Optional.ofNullable(state.cullingData())
                            .map(data -> new CullingData(data.aabb.move(pos), data.maxDistance, data.aabbExpansion))
                            .orElse(null)
            );
            World wrappedWorld = this.world.world();
            List<Player> trackedBy = getTrackedBy();
            boolean hasTrackedBy = trackedBy != null && !trackedBy.isEmpty();
            // 处理旧到新的转换
            if (previous != null) {
                // 由于entity-render的体量基本都很小，所以考虑一个特殊情况，即前后都是1个renderer，对此情况进行简化和优化
                BlockEntityElement[] previousElements = previous.elements().clone();
                if (previousElements.length == 1 && renderers.length == 1) {
                    BlockEntityElement previousElement = previousElements[0];
                    BlockEntityElementConfig<? extends BlockEntityElement> config = renderers[0];
                    outer: {
                        if (config.elementClass().isInstance(previousElement)) {
                            BlockEntityElement element = ((BlockEntityElementConfig) config).create(wrappedWorld, pos, previousElement);
                            if (element != null) {
                                elements[0] = element;
                                if (hasTrackedBy) {
                                    // 如果启用实体剔除，那么只对已经渲染的进行变换
                                    if (Config.enableEntityCulling()) {
                                        for (Player player : trackedBy) {
                                            VirtualCullableObject trackedBlockEntity = player.addTrackedBlockEntity(pos, renderer);
                                            if (trackedBlockEntity == null || trackedBlockEntity.isShown()) {
                                                element.transform(player);
                                            }
                                        }
                                    }
                                    // 否则直接变换
                                    else {
                                        for (Player player : trackedBy) {
                                            element.transform(player);
                                        }
                                    }
                                }
                                break outer;
                            }
                        }
                        BlockEntityElement element = config.create(wrappedWorld, pos);
                        elements[0] = element;
                        if (hasTrackedBy) {
                            // 如果启用实体剔除，那么只添加记录
                            if (Config.enableEntityCulling()) {
                                for (Player player : trackedBy) {
                                    player.addTrackedBlockEntity(pos, renderer);
                                }
                            }
                            // 否则直接显示
                            else {
                                for (Player player : trackedBy) {
                                    previousElement.hide(player);
                                    element.show(player);
                                }
                            }
                        }
                    }
                } else {
                    outer: for (int i = 0; i < elements.length; i++) {
                        BlockEntityElementConfig<? extends BlockEntityElement> config = renderers[i];
                        for (int j = 0; j < previousElements.length; j++) {
                            BlockEntityElement previousElement = previousElements[j];
                            if (previousElement != null && config.elementClass().isInstance(previousElement)) {
                                BlockEntityElement newElement = ((BlockEntityElementConfig) config).create(wrappedWorld, pos, previousElement);
                                if (newElement != null) {
                                    previousElements[j] = null;
                                    elements[i] = newElement;
                                    if (hasTrackedBy) {
                                        // 如果启用实体剔除，那么只对已经渲染的进行变换
                                        if (Config.enableEntityCulling()) {
                                            for (Player player : trackedBy) {
                                                VirtualCullableObject trackedBlockEntity = player.addTrackedBlockEntity(pos, renderer);
                                                if (trackedBlockEntity == null || trackedBlockEntity.isShown()) {
                                                    newElement.transform(player);
                                                }
                                            }
                                        }
                                        // 否则直接变换
                                        else {
                                            for (Player player : trackedBy) {
                                                newElement.transform(player);
                                            }
                                        }
                                    }
                                    continue outer;
                                }
                            }
                        }
                        BlockEntityElement newElement = config.create(wrappedWorld, pos);
                        elements[i] = newElement;
                        if (hasTrackedBy) {
                            if (Config.enableEntityCulling()) {
                                for (Player player : trackedBy) {
                                    player.addTrackedBlockEntity(pos, renderer);
                                }
                            } else {
                                for (Player player : trackedBy) {
                                    newElement.show(player);
                                }
                            }
                        }
                    }
                    if (hasTrackedBy) {
                        for (int i = 0; i < previousElements.length; i++) {
                            BlockEntityElement previousElement = previousElements[i];
                            if (previousElement != null) {
                                for (Player player : trackedBy) {
                                    previousElement.hide(player);
                                }
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < elements.length; i++) {
                    elements[i] = renderers[i].create(wrappedWorld, pos);
                }
                if (hasTrackedBy) {
                    if (Config.enableEntityCulling()) {
                        for (Player player : trackedBy) {
                            player.addTrackedBlockEntity(pos, renderer);
                        }
                    } else {
                        for (Player player : trackedBy) {
                            renderer.show(player);
                        }
                    }
                }
            }
            try {
                this.renderLock.writeLock().lock();
                this.constantBlockEntityRenderers.put(pos, renderer);
                return renderer;
            } finally {
                this.renderLock.writeLock().unlock();
            }
        }
        return null;
    }

    @Nullable
    public ConstantBlockEntityRenderer removeConstantBlockEntityRenderer(BlockPos pos) {
        return this.removeConstantBlockEntityRenderer(pos, true);
    }

    @Nullable
    public ConstantBlockEntityRenderer removeConstantBlockEntityRenderer(BlockPos pos, boolean hide) {
        try {
            this.renderLock.writeLock().lock();
            ConstantBlockEntityRenderer removed = this.constantBlockEntityRenderers.remove(pos);
            if (removed != null) {
                if (hide) {
                    if (Config.enableEntityCulling()) {
                        for (Player player : getTrackedBy()) {
                            player.removeTrackedBlockEntities(List.of(pos));
                        }
                    } else {
                        for (Player player : getTrackedBy()) {
                            removed.hide(player);
                        }
                    }
                }
            }
            return removed;
        } finally {
            this.renderLock.writeLock().unlock();
        }
    }

    private void removeDynamicBlockEntityRenderer(BlockPos pos) {
        try {
            this.renderLock.writeLock().lock();
            DynamicBlockEntityRenderer renderer = this.dynamicBlockEntityRenderers.remove(pos);
            if (renderer != null) {
                for (Player player : getTrackedBy()) {
                    renderer.hide(player);
                }
            }
        } finally {
            this.renderLock.writeLock().unlock();
        }
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        this.setBlockEntity(blockEntity);
        this.replaceOrCreateTickingBlockEntity(blockEntity);
        this.createDynamicBlockEntityRenderer(blockEntity);
    }

    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity removedBlockEntity = this.blockEntities.remove(blockPos);
        if (removedBlockEntity != null) {
            removedBlockEntity.setValid(false);
        }
        this.removeBlockEntityTicker(blockPos);
        this.removeDynamicBlockEntityRenderer(blockPos);
    }

    public List<Player> getTrackedBy() {
        return this.world.world.getTrackedBy(this.chunkPos);
    }

    public void activateAllBlockEntities() {
        if (this.activated) return;
        for (BlockEntity blockEntity : this.blockEntities.values()) {
            blockEntity.setValid(true);
            this.replaceOrCreateTickingBlockEntity(blockEntity);
            this.createDynamicBlockEntityRenderer(blockEntity);
        }
        for (ConstantBlockEntityRenderer renderer : this.constantBlockEntityRenderers.values()) {
            renderer.activate();
        }
        this.activated = true;
    }

    public void deactivateAllBlockEntities() {
        if (!this.activated) return;
        this.blockEntities.values().forEach(e -> e.setValid(false));
        this.constantBlockEntityRenderers.values().forEach(ConstantBlockEntityRenderer::deactivate);
        this.dynamicBlockEntityRenderers.clear();
        this.tickingSyncBlockEntitiesByPos.values().forEach((ticker) -> ticker.setTicker(DummyTickingBlockEntity.INSTANCE));
        this.tickingSyncBlockEntitiesByPos.clear();
        this.tickingAsyncBlockEntitiesByPos.values().forEach((ticker) -> ticker.setTicker(DummyTickingBlockEntity.INSTANCE));
        this.tickingAsyncBlockEntitiesByPos.clear();
        this.activated = false;
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> void replaceOrCreateTickingBlockEntity(T blockEntity) {
        ImmutableBlockState blockState = blockEntity.blockState();
        EntityBlockBehavior blockBehavior = blockState.behavior().getEntityBehavior();
        if (blockBehavior == null) {
            this.removeBlockEntityTicker(blockEntity.pos());
        } else {
            BlockEntityTicker<T> syncTicker = (BlockEntityTicker<T>) blockBehavior.createSyncBlockEntityTicker(this.world, blockState, blockEntity.type());
            if (syncTicker != null) {
                this.tickingSyncBlockEntitiesByPos.compute(blockEntity.pos(), ((pos, previousTicker) -> {
                    TickingBlockEntity newTicker = new TickingBlockEntityImpl<>(this, blockEntity, syncTicker);
                    if (previousTicker != null) {
                        previousTicker.setTicker(newTicker);
                        return previousTicker;
                    } else {
                        ReplaceableTickingBlockEntity replaceableTicker = new ReplaceableTickingBlockEntity(newTicker);
                        this.world.addSyncBlockEntityTicker(replaceableTicker);
                        return replaceableTicker;
                    }
                }));
            } else {
                this.removeSyncBlockEntityTicker(blockEntity.pos());
            }
            BlockEntityTicker<T> asyncTicker = (BlockEntityTicker<T>) blockBehavior.createAsyncBlockEntityTicker(this.world, blockState, blockEntity.type());
            if (asyncTicker != null) {
                this.tickingAsyncBlockEntitiesByPos.compute(blockEntity.pos(), ((pos, previousTicker) -> {
                    TickingBlockEntity newTicker = new TickingBlockEntityImpl<>(this, blockEntity, asyncTicker);
                    if (previousTicker != null) {
                        previousTicker.setTicker(newTicker);
                        return previousTicker;
                    } else {
                        ReplaceableTickingBlockEntity replaceableTicker = new ReplaceableTickingBlockEntity(newTicker);
                        this.world.addAsyncBlockEntityTicker(replaceableTicker);
                        return replaceableTicker;
                    }
                }));
            } else {
                this.removeAsyncBlockEntityTicker(blockEntity.pos());
            }
        }
    }

    public <T extends BlockEntity> void createDynamicBlockEntityRenderer(T blockEntity) {
        DynamicBlockEntityRenderer renderer = blockEntity.blockEntityRenderer();
        if (renderer != null) {
            DynamicBlockEntityRenderer previous;
            try {
                this.renderLock.writeLock().lock();
                previous = this.dynamicBlockEntityRenderers.put(blockEntity.pos(), renderer);
            } finally {
                this.renderLock.writeLock().unlock();
            }
            if (previous != null) {
                if (previous == renderer) {
                    return;
                }
                for (Player player : getTrackedBy()) {
                    previous.hide(player);
                    renderer.show(player);
                }
            } else {
                for (Player player : getTrackedBy()) {
                    renderer.show(player);
                }
            }
        } else {
            this.removeDynamicBlockEntityRenderer(blockEntity.pos());
        }
    }

    private void removeSyncBlockEntityTicker(BlockPos pos) {
        ReplaceableTickingBlockEntity e1 = this.tickingSyncBlockEntitiesByPos.remove(pos);
        if (e1 != null) {
            e1.setTicker(DummyTickingBlockEntity.INSTANCE);
        }
    }

    private void removeAsyncBlockEntityTicker(BlockPos pos) {
        ReplaceableTickingBlockEntity e2 = this.tickingAsyncBlockEntitiesByPos.remove(pos);
        if (e2 != null) {
            e2.setTicker(DummyTickingBlockEntity.INSTANCE);
        }
    }

    private void removeBlockEntityTicker(BlockPos pos) {
        removeSyncBlockEntityTicker(pos);
        removeAsyncBlockEntityTicker(pos);
    }

    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.pos();
        ImmutableBlockState blockState = this.getBlockState(pos);
        if (!blockState.hasBlockEntity()) {
            Debugger.BLOCK_ENTITY.debug(() -> "Failed to add invalid block entity " + blockEntity.saveAsTag() + " at " + pos);
            return;
        }
        // 设置方块实体所在世界
        blockEntity.setWorld(this.world);
        blockEntity.setValid(true);
        BlockEntity previous = this.blockEntities.put(pos, blockEntity);
        // 标记旧的方块实体无效
        if (previous != null && previous != blockEntity) {
            previous.setValid(false);
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos, boolean create) {
        BlockEntity blockEntity = this.blockEntities.get(pos);
        if (blockEntity == null) {
            if (create) {
                blockEntity = createBlockEntity(pos);
                if (blockEntity != null) {
                    this.addBlockEntity(blockEntity);
                }
            }
        } else {
            if (!blockEntity.isValid()) {
                this.blockEntities.remove(pos);
                return null;
            }
        }
        return blockEntity;
    }

    private BlockEntity createBlockEntity(BlockPos pos) {
        ImmutableBlockState blockState = this.getBlockState(pos);
        if (!blockState.hasBlockEntity()) {
            return null;
        }
        return Objects.requireNonNull(blockState.behavior().getEntityBehavior()).createBlockEntity(pos, blockState);
    }

    public Collection<BlockEntity> blockEntities() {
        return Collections.unmodifiableCollection(this.blockEntities.values());
    }

    public List<BlockPos> constantBlockEntityRendererPositions() {
        try {
            this.renderLock.readLock().lock();
            return new ArrayList<>(this.constantBlockEntityRenderers.keySet());
        } finally {
            this.renderLock.readLock().unlock();
        }
    }

    public boolean dirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isEmpty() {
        if (!this.blockEntities.isEmpty()) return false;
        for (CESection section : this.sections) {
            if (section != null && !section.statesContainer().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void fillEmptySection() {
        for (int i = 0; i < this.sections.length; ++i) {
            if (this.sections[i] == null) {
                this.sections[i] = new CESection(this.world.worldHeight().getSectionYFromSectionIndex(i),
                        new PalettedContainer<>(null, EmptyBlock.STATE, PalettedContainer.PaletteProvider.CUSTOM_BLOCK_STATE));
            }
        }
    }

    public void setBlockState(BlockPos pos, ImmutableBlockState state) {
        this.setBlockState(pos.x(), pos.y(), pos.z(), state);
    }

    public void setBlockState(int x, int y, int z, ImmutableBlockState state) {
        int index = sectionIndex(SectionPos.blockToSectionCoord(y));
        CESection section = this.sections[index];
        if (section == null) {
            return;
        }
        ImmutableBlockState previous = section.setBlockState((y & 15) << 8 | (z & 15) << 4 | x & 15, state);
        if (previous != state) {
            setDirty(true);
        }
    }

    @NotNull
    public ImmutableBlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.x(), pos.y(), pos.z());
    }

    @NotNull
    public ImmutableBlockState getBlockState(int x, int y, int z) {
        int index = sectionIndex(SectionPos.blockToSectionCoord(y));
        CESection section = this.sections[index];
        if (section == null) {
            return EmptyBlock.STATE;
        }
        return section.getBlockState((y & 15) << 8 | (z & 15) << 4 | x & 15);
    }

    @Nullable
    public CESection sectionByIndex(int index) {
        return this.sections[index];
    }

    @NotNull
    public CESection sectionById(int sectionId) {
        return this.sections[sectionIndex(sectionId)];
    }

    public int sectionIndex(int sectionId) {
        return sectionId - this.worldHeightAccessor.getMinSection();
    }

    public int sectionY(int sectionIndex) {
        return sectionIndex + this.worldHeightAccessor.getMinSection();
    }

    @NotNull
    public CEWorld world() {
        return this.world;
    }

    @NotNull
    public ChunkPos chunkPos() {
        return this.chunkPos;
    }

    @NotNull
    public CESection[] sections() {
        return this.sections;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void load() {
        if (this.loaded) return;
        this.world.addLoadedChunk(this);
        this.loaded = true;
    }

    public void unload() {
        if (!this.loaded) return;
        this.world.removeLoadedChunk(this);
        this.loaded = false;
    }
}
