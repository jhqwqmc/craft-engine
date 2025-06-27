package net.momirealms.craftengine.bukkit.block.behavior;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBuiltInRegistries;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.PlaceBlockBlockPlaceContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.World;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class FacingTriggerableBlockBehavior extends BukkitBlockBehavior {
    protected static final Set<Key> DEFAULT_BLACKLIST_BLOCKS = ObjectOpenHashSet.of(
            Key.of("minecraft:bedrock"),
            Key.of("minecraft:end_portal_frame"),
            Key.of("minecraft:end_portal"),
            Key.of("minecraft:nether_portal"),
            Key.of("minecraft:barrier"),
            Key.of("minecraft:command_block"),
            Key.of("minecraft:chain_command_block"),
            Key.of("minecraft:repeating_command_block"),
            Key.of("minecraft:structure_block"),
            Key.of("minecraft:end_gateway"),
            Key.of("minecraft:jigsaw"),
            Key.of("minecraft:structure_void"),
            Key.of("minecraft:test_instance_block"),
            Key.of("minecraft:moving_piston"),
            Key.of("minecraft:test_block"),
            Key.of("minecraft:light")
    );
    protected final Property<Direction> facingProperty;
    protected final Property<Boolean> triggeredProperty;
    protected final Set<Key> blocks;
    protected final boolean whitelistMode;

    public FacingTriggerableBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered, Set<Key> blocks, boolean whitelistMode) {
        super(customBlock);
        this.facingProperty = facing;
        this.triggeredProperty = triggered;
        this.blocks = blocks;
        this.whitelistMode = whitelistMode;
    }

    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        boolean hasNeighborSignal = FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, pos);
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return;
        boolean triggeredValue = blockState.get(this.triggeredProperty);
        if (hasNeighborSignal && !triggeredValue) {
            // FastNMS.INSTANCE.method$LevelAccessor$scheduleBlockTick(level, pos, thisBlock, 1, this.getTickPriority()); // 鬼知道为什么这个无法触发 tick
            World world = null;
            int x = 0;
            int z = 0;
            if (VersionHelper.isFolia()) {
                world = FastNMS.INSTANCE.method$Level$getCraftWorld(level);
                x = FastNMS.INSTANCE.field$Vec3i$x(pos) >> 4;
                z = FastNMS.INSTANCE.field$Vec3i$z(pos) >> 4;
            }
            CraftEngine.instance().scheduler().sync().runLater(() -> tick(state, level, pos), 1, world, x, z);
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState.with(this.triggeredProperty, true).customBlockState().handle(), 2);
        } else if (!hasNeighborSignal && triggeredValue) {
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState.with(this.triggeredProperty, false).customBlockState().handle(), 2);
        }
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        if (context instanceof PlaceBlockBlockPlaceContext placeContext) {
            return state.owner().value().defaultState().with(this.facingProperty, placeContext.getNearestLookingDirection().opposite());
        }
        Direction direction = DirectionUtils.fromNMSDirection(FastNMS.INSTANCE.method$Direction$getOpposite(FastNMS.INSTANCE.method$Direction$orderedByNearest(context.getPlayer().serverPlayer())[0]));
        return state.owner().value().defaultState().with(this.facingProperty, direction);
    }

    protected boolean blockCheckByBlockState(Object blockState) {
        if (blockState == null || FastNMS.INSTANCE.method$BlockStateBase$isAir(blockState)) {
            return false;
        }
        Key blockId = Optional.ofNullable(BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState)))
                .filter(state -> !state.isEmpty())
                .map(state -> state.owner().value().id())
                .orElseGet(() -> FastNMS.INSTANCE.method$DefaultedRegistry$getKey(MBuiltInRegistries.BLOCK, FastNMS.INSTANCE.method$BlockState$getBlock(blockState)));
        return blockCheckByKey(blockId);
    }

    protected boolean blockCheckByKey(Key blockId) {
        return this.blocks.contains(blockId) == this.whitelistMode;
    }

    protected abstract Object getTickPriority();

    protected abstract void tick(Object state, Object level, Object pos);
}
