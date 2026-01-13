package net.momirealms.craftengine.core.world.generation.feature.modifier;

import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class CountPlacement extends PlacementModifier {
    public static final PlacementModifierFactory<CountPlacement> FACTORY = new Factory();
    private final NumberProvider count;

    private CountPlacement(NumberProvider count) {
        this.count = count;
    }

    @Override
    public Stream<BlockPos> getPositions(GeneratingWorld world, BlockPos origin, RandomSource random) {
        return IntStream.range(0, this.count.getInt(random)).mapToObj(i -> origin);
    }

    private static class Factory implements PlacementModifierFactory<CountPlacement> {

        @Override
        public CountPlacement create(Map<String, Object> args) {
            return new CountPlacement(NumberProviders.fromObject(args.get("count")));
        }
    }
}
