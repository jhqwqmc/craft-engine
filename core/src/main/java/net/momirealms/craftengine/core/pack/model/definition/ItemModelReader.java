package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface ItemModelReader {

    ItemModel read(JsonObject json);
}
