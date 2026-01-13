package net.momirealms.craftengine.core.world.generation.predicate;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.world.Vec3i;

final class ReplaceablePredicate extends OffsetBlockPredicate {

    private ReplaceablePredicate(Vec3i offset) {
        super(offset);
    }

    @Override
    protected boolean test(BlockStateWrapper state) {
        return state.replaceable();
    }
}
