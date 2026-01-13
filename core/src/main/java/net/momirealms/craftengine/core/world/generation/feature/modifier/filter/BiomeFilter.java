package net.momirealms.craftengine.core.world.generation.feature.modifier.filter;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.random.RandomSource;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.GeneratingWorld;
import net.momirealms.craftengine.core.world.generation.feature.modifier.PlacementModifierFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class BiomeFilter extends PlacementFilter {
    public static final PlacementModifierFactory<BiomeFilter> FACTORY = new Factory();
    private final Set<Key> biomes;

    private BiomeFilter(Set<Key> biomes) {
        this.biomes = biomes;
    }

    @Override
    protected boolean test(GeneratingWorld world, BlockPos origin, RandomSource random) {
        return this.biomes.contains(world.getNoiseBiome(origin));
    }

    private static class Factory implements PlacementModifierFactory<BiomeFilter> {

        @Override
        public BiomeFilter create(Map<String, Object> args) {
            return new BiomeFilter(MiscUtils.getAsStringList(args.get("biome")).stream().map(Key::of).collect(Collectors.toSet()));
        }
    }
}
