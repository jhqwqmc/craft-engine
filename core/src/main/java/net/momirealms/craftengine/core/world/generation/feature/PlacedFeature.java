package net.momirealms.craftengine.core.world.generation.feature;

import net.momirealms.craftengine.core.util.MutableBoolean;
import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.generation.feature.modifier.PlacementModifier;

import java.util.stream.Stream;

public abstract class PlacedFeature {
    protected final PlacementModifier[] modifiers;

    protected PlacedFeature(PlacementModifier[] modifiers) {
        this.modifiers = modifiers;
    }

    public boolean place(GeneratingWorld world, BlockPos origin, RandomSource random) {
        Stream<BlockPos> stream = Stream.of(origin);
        for (PlacementModifier modifier : this.modifiers) {
            stream = stream.flatMap((pos) -> modifier.getPositions(world, pos, random));
        }
        MutableBoolean placed = new MutableBoolean(false);
        stream.forEach((placedPos) -> {
            if (placeFeature(world, placedPos, random)) {
                placed.set(true);
            }
        });
        return placed.booleanValue();
    }

    protected abstract boolean placeFeature(GeneratingWorld world, BlockPos origin, RandomSource random);
}
