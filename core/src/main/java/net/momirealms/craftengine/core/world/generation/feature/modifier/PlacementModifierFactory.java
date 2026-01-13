package net.momirealms.craftengine.core.world.generation.feature.modifier;

import java.util.Map;

public interface PlacementModifierFactory<T extends PlacementModifier> {

    T create(Map<String, Object> args);
}
