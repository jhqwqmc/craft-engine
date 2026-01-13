package net.momirealms.craftengine.core.world.generation.feature.modifier;

import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;

import java.util.Map;
import java.util.stream.Stream;

public final class RandomOffsetPlacement extends PlacementModifier {
    public static final PlacementModifierFactory<RandomOffsetPlacement> FACTORY = new Factory();
    private final NumberProvider xzSpread;
    private final NumberProvider ySpread;

    private RandomOffsetPlacement(NumberProvider xzSpread, NumberProvider ySpread) {
        this.xzSpread = xzSpread;
        this.ySpread = ySpread;
    }

    @Override
    public Stream<BlockPos> getPositions(GeneratingWorld world, BlockPos origin, RandomSource random) {
        int x = origin.x + this.xzSpread.getInt(random);
        int y = origin.y + this.ySpread.getInt(random);
        int z = origin.z + this.xzSpread.getInt(random);
        return Stream.of(new BlockPos(x, y, z));
    }

    private static class Factory implements PlacementModifierFactory<RandomOffsetPlacement> {

        @Override
        public RandomOffsetPlacement create(Map<String, Object> args) {
            NumberProvider xzSpread = NumberProviders.fromObject(args.get("xz-spread"));
            NumberProvider ySpread = NumberProviders.fromObject(args.get("y-spread"));
            return new RandomOffsetPlacement(xzSpread, ySpread);
        }
    }
}
