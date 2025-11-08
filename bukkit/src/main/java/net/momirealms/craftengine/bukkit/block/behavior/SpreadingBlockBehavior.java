package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MTagKeys;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.*;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.BooleanProperty;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class SpreadingBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final int spreadLight;
    private final LazyReference<Object> spreadBlock;

    public SpreadingBlockBehavior(CustomBlock customBlock, int spreadLight, String spreadBlock) {
        super(customBlock);
        this.spreadLight = spreadLight;
        this.spreadBlock = LazyReference.lazyReference(() -> Objects.requireNonNull(BukkitBlockManager.instance().createBlockState(spreadBlock)).literalObject());
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        if (args[updateShape$direction] != CoreReflections.instance$Direction$UP) return superMethod.call();
        return BlockStateUtils.toBlockStateWrapper(args[0]).withProperty("snowy", String.valueOf(isSnowySetting(args[updateShape$neighborState]))).literalObject();
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        BooleanProperty snowy = (BooleanProperty) this.block().getProperty("snowy");
        if (snowy == null) return state;
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(context.getLevel().serverWorld(), LocationUtils.toBlockPos(context.getClickedPos().above()));
        return state.with(snowy, isSnowySetting(blockState));
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        if (!canBeGrass(state, level, pos)) {
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, this.spreadBlock.get(), 3);
            return;
        }
        if (FastNMS.INSTANCE.method$LevelReader$getMaxLocalRawBrightness(level, FastNMS.INSTANCE.method$BlockPos$relative(pos, CoreReflections.instance$Direction$UP)) < this.spreadLight) return;
        ImmutableBlockState blockState = this.block().defaultState();
        BooleanProperty snowy = (BooleanProperty) this.block().getProperty("snowy");
        for (int i = 0; i < 4; i++) {
            Object blockPos = FastNMS.INSTANCE.method$BlockPos$offset(pos, RandomUtils.generateRandomInt(-1, 2), RandomUtils.generateRandomInt(-3, 2), RandomUtils.generateRandomInt(-1, 2));
            if (FastNMS.INSTANCE.method$BlockStateBase$isBlock(FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, blockPos), FastNMS.INSTANCE.method$BlockState$getBlock(this.spreadBlock.get())) && canPropagate(state, level, blockPos)) {
                if (snowy != null) blockState = blockState.with(snowy, FastNMS.INSTANCE.method$BlockStateBase$isBlock(FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, FastNMS.INSTANCE.method$BlockPos$relative(pos, CoreReflections.instance$Direction$UP)), MBlocks.SNOW));
                FastNMS.INSTANCE.method$LevelWriter$setBlock(level, blockPos, blockState.customBlockState().literalObject(), 3);
            }
        }
    }

    private static boolean canBeGrass(Object state, Object level, Object pos) {
        Object blockPos = FastNMS.INSTANCE.method$BlockPos$relative(pos, CoreReflections.instance$Direction$UP);
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, blockPos);
        if (FastNMS.INSTANCE.method$BlockStateBase$isBlock(blockState, MBlocks.SNOW) && ((Integer) FastNMS.INSTANCE.method$StateHolder$getValue(blockState, CoreReflections.instance$SnowLayerBlock$LAYERS)) == 1) return true;
        else if (FastNMS.INSTANCE.field$FluidState$amount(FastNMS.INSTANCE.field$BlockBehaviour$BlockStateBase$fluidState(blockState)) == 8) return false;
        else {
            return FastNMS.INSTANCE.method$LightEngine$getLightBlockInto(
                    VersionHelper.isOrAbove1_21_2() ? null : level,
                    state,
                    VersionHelper.isOrAbove1_21_2() ? null : pos,
                    blockState,
                    VersionHelper.isOrAbove1_21_2() ? null : blockPos,
                    CoreReflections.instance$Direction$UP,
                    FastNMS.INSTANCE.method$BlockBehaviour$BlockStateBase$getLightBlock(
                            blockState,
                            VersionHelper.isOrAbove1_21_2() ? null : level,
                            VersionHelper.isOrAbove1_21_2() ? null : pos
                    )
            ) < 15;
        }
    }

    private static boolean isSnowySetting(Object state) {
        return FastNMS.INSTANCE.method$BlockStateBase$is(state, MTagKeys.Block$SNOW);
    }

    private static boolean canPropagate(Object state, Object level, Object pos) {
        Object blockPos = FastNMS.INSTANCE.method$BlockPos$relative(pos, CoreReflections.instance$Direction$UP);
        return canBeGrass(state, level, pos) && !FastNMS.INSTANCE.method$FluidState$is(FastNMS.INSTANCE.method$BlockGetter$getFluidState(level, blockPos), MTagKeys.Fluid$WATER);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            int spreadLight = ResourceConfigUtils.getAsInt(arguments.getOrDefault("spread-light", 9), "spread-light");
            String spreadBlock = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.getOrDefault("spread-block", "minecraft:dirt"), "warning.config.block.behavior.spreading.missing_spread_block");
            return new SpreadingBlockBehavior(block, spreadLight, spreadBlock);
        }
    }
}
