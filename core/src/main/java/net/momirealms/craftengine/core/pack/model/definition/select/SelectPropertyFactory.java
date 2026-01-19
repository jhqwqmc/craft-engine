package net.momirealms.craftengine.core.pack.model.definition.select;

import java.util.Map;

public interface SelectPropertyFactory<T extends SelectProperty> {

    T create(Map<String, Object> arguments);
}
