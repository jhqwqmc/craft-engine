package net.momirealms.craftengine.core.pack.model.definition.condition;

import java.util.Map;

public interface ConditionPropertyFactory<T extends ConditionProperty> {

    T create(Map<String, Object> arguments);
}
