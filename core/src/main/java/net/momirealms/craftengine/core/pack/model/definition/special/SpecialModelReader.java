package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;

public interface SpecialModelReader {

    SpecialModel read(JsonObject json);
}
