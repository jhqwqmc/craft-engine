package net.momirealms.craftengine.core.world.generation.predicate;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.Vec3i;

import java.util.Set;

final class MatchFluidPredicate extends OffsetBlockPredicate {
    private final Set<Key> fluids;

    private MatchFluidPredicate(Vec3i offset, Set<Key> fluids) {
        super(offset);
        this.fluids = fluids;
    }

    @Override
    protected boolean test(BlockStateWrapper state) {
        return this.fluids.contains(state.fluidState());
    }
}
