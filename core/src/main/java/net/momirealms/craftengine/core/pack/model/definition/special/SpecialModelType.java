package net.momirealms.craftengine.core.pack.model.definition.special;

import net.momirealms.craftengine.core.util.Key;

public record SpecialModelType(Key id, SpecialModelFactory factory, SpecialModelReader reader) {
}
