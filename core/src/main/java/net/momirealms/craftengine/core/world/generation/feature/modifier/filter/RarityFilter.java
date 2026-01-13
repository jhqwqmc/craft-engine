package net.momirealms.craftengine.core.world.generation.feature.modifier.filter;

import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.generation.feature.modifier.PlacementModifierFactory;

import java.util.Map;

public final class RarityFilter extends PlacementFilter {
    public static final PlacementModifierFactory<RarityFilter> FACTORY = new Factory();
    private final int chance;

    private RarityFilter(int chance) {
        this.chance = chance;
    }

    @Override
    protected boolean test(GeneratingWorld world, BlockPos origin, RandomSource random) {
        return random.nextFloat() < 1.0F / (float) this.chance;
    }

    private static class Factory implements PlacementModifierFactory<RarityFilter> {

        @Override
        public RarityFilter create(Map<String, Object> args) {
            return new RarityFilter(ResourceConfigUtils.getAsInt(args.get("chance"), "chance"));
        }
    }
}
