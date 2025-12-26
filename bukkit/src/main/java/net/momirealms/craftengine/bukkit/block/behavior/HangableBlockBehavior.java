package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MFluids;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.IsPathFindableBlockBehavior;
import net.momirealms.craftengine.core.block.properties.BooleanProperty;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;

// todo 修改
public class HangableBlockBehavior extends BukkitBlockBehavior implements IsPathFindableBlockBehavior {
    public static final Key ID = Key.from("craftengine:hangable_block");
    public static final BlockBehaviorFactory FACTORY = new Factory();
    private final BooleanProperty hanging;

    public HangableBlockBehavior(CustomBlock customBlock, BooleanProperty hanging) {
        super(customBlock);
        this.hanging = hanging;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        BooleanProperty hanging = (BooleanProperty) state.owner().value().getProperty("hanging");
        if (hanging == null) return state;
        @Nullable BooleanProperty waterlogged = (BooleanProperty) state.owner().value().getProperty("waterlogged");
        Object world = context.getLevel().serverWorld();
        Object blockPos = LocationUtils.toBlockPos(context.getClickedPos());
        Object fluidType = FastNMS.INSTANCE.method$FluidState$getType(FastNMS.INSTANCE.method$BlockGetter$getFluidState(world, blockPos));
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.axis() != Direction.Axis.Y) continue;
            ImmutableBlockState blockState = state.with(hanging, direction == Direction.UP);
            if (!FastNMS.INSTANCE.method$BlockStateBase$canSurvive(blockState.customBlockState().literalObject(), world, blockPos)) continue;
            return waterlogged != null ? blockState.with(waterlogged, fluidType == MFluids.WATER) : blockState;
        }
        return state;
    }

    @Override
    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object world = args[1];
        Object blockPos = args[2];
        ImmutableBlockState blockState = BlockStateUtils.getOptionalCustomBlockState(state).orElse(null);
        if (blockState == null) return false;
        BooleanProperty hangingProperty = (BooleanProperty) blockState.owner().value().getProperty("hanging");
        if (hangingProperty == null) return false;
        Boolean hanging = blockState.get(hangingProperty);
        Object relativePos = FastNMS.INSTANCE.method$BlockPos$relative(blockPos, hanging ? CoreReflections.instance$Direction$UP : CoreReflections.instance$Direction$DOWN);
        return FastNMS.INSTANCE.method$Block$canSupportCenter(world, relativePos, hanging ? CoreReflections.instance$Direction$DOWN : CoreReflections.instance$Direction$UP);
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
        if (state == null) return MBlocks.AIR$defaultState;
        @Nullable BooleanProperty waterlogged = (BooleanProperty) state.owner().value().getProperty("waterlogged");
        if (waterlogged != null && state.get(waterlogged)) {
            FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleFluidTick(args[updateShape$level], args[updateShape$blockPos], MFluids.WATER, 5);
        }
        BooleanProperty hanging = (BooleanProperty) state.owner().value().getProperty("hanging");
        if (hanging == null) return MBlocks.AIR$defaultState;
        if ((state.get(hanging) ? CoreReflections.instance$Direction$UP : CoreReflections.instance$Direction$DOWN) == args[updateShape$direction]
                && !FastNMS.INSTANCE.method$BlockStateBase$canSurvive(args[0], args[updateShape$level], args[updateShape$blockPos])) {
            return MBlocks.AIR$defaultState;
        }
        return superMethod.call();
    }

    @Override
    public boolean isPathFindable(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        return false;
    }

    private static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            BooleanProperty hanging = (BooleanProperty) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("hanging"), "warning.config.block.behavior.hangable.missing_hanging");
            return new HangableBlockBehavior(block, hanging);
        }
    }
}
