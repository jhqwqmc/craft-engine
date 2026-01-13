package net.momirealms.craftengine.core.world.generation.feature.modifier;

import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;

import java.util.Map;
import java.util.stream.Stream;

public final class SetHeightPlacement extends PlacementModifier {
    public static final PlacementModifierFactory<SetHeightPlacement> FACTORY = new Factory();
    private final NumberProvider height;

    private SetHeightPlacement(NumberProvider height) {
        this.height = height;
    }

    @Override
    public Stream<BlockPos> getPositions(GeneratingWorld world, BlockPos origin, RandomSource random) {
        return Stream.of(new BlockPos(origin.x, this.height.getInt(random), origin.z));
    }

    private static class Factory implements PlacementModifierFactory<SetHeightPlacement> {

        @Override
        public SetHeightPlacement create(Map<String, Object> args) {
            NumberProvider height = NumberProviders.fromObject(args.get("height"));
            return new SetHeightPlacement(height);
        }
    }
}
