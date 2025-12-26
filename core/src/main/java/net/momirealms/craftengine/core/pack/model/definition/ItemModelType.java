package net.momirealms.craftengine.core.pack.model.definition;

import net.momirealms.craftengine.core.util.Key;

public record ItemModelType(Key id, ItemModelFactory factory, ItemModelReader reader) {
}
