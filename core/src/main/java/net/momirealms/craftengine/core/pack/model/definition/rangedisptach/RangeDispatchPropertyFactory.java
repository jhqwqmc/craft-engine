package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import java.util.Map;

public interface RangeDispatchPropertyFactory<T extends RangeDispatchProperty> {

    T create(Map<String, Object> arguments);
}
