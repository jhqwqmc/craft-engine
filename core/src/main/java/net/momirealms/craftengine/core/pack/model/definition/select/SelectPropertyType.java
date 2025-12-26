package net.momirealms.craftengine.core.pack.model.definition.select;

import net.momirealms.craftengine.core.util.Key;

public record SelectPropertyType(Key id, SelectPropertyFactory factory, SelectPropertyReader reader) {
}
