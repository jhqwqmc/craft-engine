package net.momirealms.craftengine.core.world.generation.feature;

import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.generation.feature.modifier.PlacementModifier;

public class OrePlacedFeature extends PlacedFeature {

    public OrePlacedFeature(PlacementModifier[] modifiers) {
        super(modifiers);
    }

    @Override
    protected boolean placeFeature(GeneratingWorld world, BlockPos origin, RandomSource random) {
        return false;
    }
}
