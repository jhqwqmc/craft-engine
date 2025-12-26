package net.momirealms.craftengine.core.pack.model.definition.select;

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

public final class DisplayContextSelectProperty implements SelectProperty {
    public static final Key ID = Key.of("minecraft:display_context");
    public static final DisplayContextSelectProperty INSTANCE = new DisplayContextSelectProperty();
    public static final SelectPropertyFactory FACTORY = new Factory();
    public static final SelectPropertyReader READER = new Reader();

    private DisplayContextSelectProperty() {}

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", ID.asMinimalString());
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

    private static class Factory implements SelectPropertyFactory {
        @Override
        public SelectProperty create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements SelectPropertyReader {
        @Override
        public SelectProperty read(JsonObject json) {
            return INSTANCE;
        }
    }
}
