package net.momirealms.craftengine.core.pack.model.definition.select;

import java.util.Map;

public interface SelectPropertyFactory {

    SelectProperty create(Map<String, Object> arguments);
}
