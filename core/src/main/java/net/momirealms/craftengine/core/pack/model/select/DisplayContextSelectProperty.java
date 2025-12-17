package net.momirealms.craftengine.core.pack.model.select;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.pack.revision.Revisions;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DisplayContextSelectProperty implements SelectProperty {
    public static final DisplayContextSelectProperty INSTANCE = new DisplayContextSelectProperty();
    public static final Factory FACTORY = new Factory();
    public static final Reader READER = new Reader();

    @Override
    public Key type() {
        return SelectProperties.DISPLAY_CONTEXT;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", type().toString());
    }

    @Override
    public List<Revision> revisions(JsonElement element) {
        if (element instanceof JsonPrimitive primitive && primitive.isString() && primitive.getAsString().equals("on_shelf")) {
            return List.of(Revisions.SINCE_1_21_9);
        }
        return List.of();
    }

    @Override
    public @Nullable JsonElement remap(JsonElement element, MinecraftVersion version) {
        if (version.isBelow(MinecraftVersion.V1_21_9) && element instanceof JsonPrimitive primitive && primitive.isString()) {
            if (primitive.getAsString().equals("on_shelf")) {
                return null;
            }
        }
        return element;
    }

    public static class Factory implements SelectPropertyFactory {
        @Override
        public SelectProperty create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    public static class Reader implements SelectPropertyReader {
        @Override
        public SelectProperty read(JsonObject json) {
            return INSTANCE;
        }
    }
}
