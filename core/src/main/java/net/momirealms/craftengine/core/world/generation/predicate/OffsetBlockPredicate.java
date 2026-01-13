package net.momirealms.craftengine.core.world.generation.predicate;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.Vec3i;

abstract class OffsetBlockPredicate implements BlockPredicate {
    protected final Vec3i offset;

    protected OffsetBlockPredicate(Vec3i offset) {
        this.offset = offset;
    }

    @Override
    public boolean test(GeneratingWorld world, BlockPos pos) {
        return this.test(world.getBlockState(pos.x + offset.x, pos.y + offset.y, pos.z + offset.z));
    }

    protected abstract boolean test(BlockStateWrapper state);
}
