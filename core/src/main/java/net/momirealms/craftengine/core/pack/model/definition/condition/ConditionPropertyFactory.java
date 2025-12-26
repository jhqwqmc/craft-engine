package net.momirealms.craftengine.core.pack.model.definition.condition;

import java.util.Map;

public interface ConditionPropertyFactory {

    ConditionProperty create(Map<String, Object> arguments);
}
