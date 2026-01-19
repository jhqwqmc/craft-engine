package net.momirealms.craftengine.core.pack.model.definition;

import java.util.Map;

@FunctionalInterface
public interface ItemModelFactory<T extends ItemModel> {

    T create(Map<String, Object> arguments);
}
