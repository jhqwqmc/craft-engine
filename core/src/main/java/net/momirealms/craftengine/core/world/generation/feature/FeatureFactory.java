package net.momirealms.craftengine.core.world.generation.feature;

import java.util.Map;

public interface FeatureFactory<T extends PlacedFeature> {

    T create(Map<String, Object> args);
}
