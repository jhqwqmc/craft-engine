package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;

import java.util.Map;
import java.util.concurrent.Callable;

public class DirectionalAttachedBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<?> facingProperty;
    private final boolean isSixDirection;

    public DirectionalAttachedBlockBehavior(CustomBlock customBlock, Property<?> facingProperty, boolean isSixDirection) {
        super(customBlock);
        this.facingProperty = facingProperty;
        this.isSixDirection = isSixDirection;
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
        if (state == null) return args[0];
        DirectionalAttachedBlockBehavior behavior = state.behavior().getAs(DirectionalAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return state;
        boolean flag;
        if (isSixDirection) {
            Direction direction = DirectionUtils.fromNMSDirection(args[updateShape$direction]).opposite();
            flag = direction == state.get(behavior.facingProperty);
        } else {
            HorizontalDirection direction = DirectionUtils.fromNMSDirection(args[updateShape$direction]).opposite().toHorizontalDirection();
            flag = direction == state.get(behavior.facingProperty);
        }
        return flag && !FastNMS.INSTANCE.method$BlockStateBase$canSurvive(args[0], args[updateShape$level], args[updateShape$blockPos]) ? MBlocks.AIR$defaultState : args[0];
    }

    @Override
    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
        if (state == null) return false;
        DirectionalAttachedBlockBehavior behavior = state.behavior().getAs(DirectionalAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return false;
        Direction direction;
        if (isSixDirection) {
            direction = ((Direction) state.get(behavior.facingProperty)).opposite();
        } else {
            direction = ((HorizontalDirection) state.get(behavior.facingProperty)).opposite().toDirection();
        }
        BlockPos blockPos = LocationUtils.fromBlockPos(args[2]).relative(direction);
        Object nmsPos = LocationUtils.toBlockPos(blockPos);
        Object nmsState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(args[1], nmsPos);
        return FastNMS.INSTANCE.method$BlockStateBase$isFaceSturdy(nmsState, args[1], nmsPos, DirectionUtils.toNMSDirection(direction), CoreReflections.instance$SupportType$FULL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        DirectionalAttachedBlockBehavior behavior = state.behavior().getAs(DirectionalAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return null;
        World level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (isSixDirection) {
                state = state.with((Property<Direction>) behavior.facingProperty, direction.opposite());
                if (FastNMS.INSTANCE.method$BlockStateBase$canSurvive(state.customBlockState().literalObject(), level.serverWorld(), LocationUtils.toBlockPos(clickedPos))) {
                    return state;
                }
            } else if (direction.axis().isHorizontal()) {
                state = state.with((Property<HorizontalDirection>) behavior.facingProperty, direction.opposite().toHorizontalDirection());
                if (FastNMS.INSTANCE.method$BlockStateBase$canSurvive(state.customBlockState().literalObject(), level.serverWorld(), LocationUtils.toBlockPos(clickedPos))) {
                    return state;
                }
            }
        }
        return null;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<?> facing = ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.surface_attached.missing_facing");
            boolean isHorizontalDirection = facing.valueClass() == HorizontalDirection.class;
            boolean isDirection = facing.valueClass() == Direction.class;
            if (!(isHorizontalDirection || isDirection)) {
                throw new LocalizedResourceConfigException("warning.config.block.behavior.surface_attached.missing_facing");
            }
            return new DirectionalAttachedBlockBehavior(block, facing, isDirection);
        }
    }
}
