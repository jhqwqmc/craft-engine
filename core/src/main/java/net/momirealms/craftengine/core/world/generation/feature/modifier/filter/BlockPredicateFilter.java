package net.momirealms.craftengine.core.world.generation.feature.modifier.filter;

import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.generation.predicate.BlockPredicate;

public final class BlockPredicateFilter extends PlacementFilter {
    private final BlockPredicate predicate;

    public BlockPredicateFilter(BlockPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    protected boolean test(GeneratingWorld world, BlockPos origin, RandomSource random) {
        return this.predicate.test(world, origin);
    }
}
