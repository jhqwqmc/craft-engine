package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;
import java.util.Map;

public final class SimpleSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<SimpleSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<SimpleSpecialModel> READER = new Reader();
    private final Key type;

    public SimpleSpecialModel(Key type) {
        this.type = type;
    }

    public Key type() {
        return this.type;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.type.asMinimalString());
        return json;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    private static class Factory implements SpecialModelFactory<SimpleSpecialModel> {
        @Override
        public SimpleSpecialModel create(Map<String, Object> arguments) {
            Key type = Key.of(arguments.get("type").toString());
            return new SimpleSpecialModel(type);
        }
    }

    private static class Reader implements SpecialModelReader<SimpleSpecialModel> {
        @Override
        public SimpleSpecialModel read(JsonObject json) {
            Key type = Key.of(json.get("type").getAsString());
            return new SimpleSpecialModel(type);
        }
    }
}
