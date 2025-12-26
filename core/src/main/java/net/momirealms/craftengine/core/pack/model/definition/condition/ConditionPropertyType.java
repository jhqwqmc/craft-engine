package net.momirealms.craftengine.core.pack.model.definition.condition;

import net.momirealms.craftengine.core.util.Key;

public record ConditionPropertyType(Key id, ConditionPropertyFactory factory, ConditionPropertyReader reader) {
}
