package net.momirealms.craftengine.core.world.generation.feature.modifier;

import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;

import java.util.stream.Stream;

public abstract class PlacementModifier {

    public abstract Stream<BlockPos> getPositions(GeneratingWorld world, BlockPos origin, RandomSource random);
}
