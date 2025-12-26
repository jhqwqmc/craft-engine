package net.momirealms.craftengine.core.pack.model.definition;

import java.util.Map;

@FunctionalInterface
public interface ItemModelFactory {

    ItemModel create(Map<String, Object> arguments);
}
