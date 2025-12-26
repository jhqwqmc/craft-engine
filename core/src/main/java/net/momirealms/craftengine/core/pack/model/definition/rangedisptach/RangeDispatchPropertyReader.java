package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;

public interface RangeDispatchPropertyReader {
    
    RangeDispatchProperty read(JsonObject json);
}
