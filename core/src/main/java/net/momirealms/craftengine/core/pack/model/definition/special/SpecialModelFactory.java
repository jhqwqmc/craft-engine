package net.momirealms.craftengine.core.pack.model.definition.special;

import java.util.Map;

public interface SpecialModelFactory<T extends SpecialModel> {

    T create(Map<String, Object> arguments);
}
