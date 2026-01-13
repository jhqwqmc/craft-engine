package net.momirealms.craftengine.core.world.generation.predicate;

import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;

@FunctionalInterface
public interface BlockPredicate {

    boolean test(GeneratingWorld world, BlockPos pos);
}
