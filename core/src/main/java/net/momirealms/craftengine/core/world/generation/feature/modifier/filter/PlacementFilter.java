package net.momirealms.craftengine.core.world.generation.feature.modifier.filter;

import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.generation.feature.modifier.PlacementModifier;

import java.util.stream.Stream;

public abstract class PlacementFilter extends PlacementModifier {

    @Override
    public Stream<BlockPos> getPositions(GeneratingWorld world, BlockPos origin, RandomSource random) {
        return this.test(world, origin, random) ? Stream.of(origin) : Stream.of();
    }

    protected abstract boolean test(GeneratingWorld world, BlockPos origin, RandomSource random);
}
