package net.momirealms.craftengine.core.world.generation.feature.modifier;

import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;

import java.util.Map;
import java.util.stream.Stream;

public final class InSquarePlacement extends PlacementModifier {
    public static final PlacementModifierFactory<InSquarePlacement> FACTORY = new Factory();
    public static final InSquarePlacement INSTANCE = new InSquarePlacement();

    private InSquarePlacement() {}

    @Override
    public Stream<BlockPos> getPositions(GeneratingWorld world, BlockPos origin, RandomSource random) {
        int x = origin.x + random.nextInt(16);
        int z = origin.z + random.nextInt(16);
        return Stream.of(new BlockPos(x, origin.y, z));
    }

    private static class Factory implements PlacementModifierFactory<InSquarePlacement> {

        @Override
        public InSquarePlacement create(Map<String, Object> args) {
            return INSTANCE;
        }
    }
}
