package net.momirealms.craftengine.core.pack.model.definition.tint;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import org.incendo.cloud.type.Either;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class SimpleDefaultTint implements Tint {
    public static final TintFactory<SimpleDefaultTint> FACTORY = new Factory();
    public static final TintReader<SimpleDefaultTint> READER = new Reader();
    private final Either<Integer, List<Float>> defaultValue;
    private final Key type;

    public SimpleDefaultTint(Key type, @Nullable Either<Integer, List<Float>> defaultValue) {
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public Either<Integer, List<Float>> defaultValue() {
        return this.defaultValue;
    }

    public Key type() {
        return this.type;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.type.asMinimalString());
        applyAnyTint(json, this.defaultValue, "default");
        return json;
    }

    private static class Factory implements TintFactory<SimpleDefaultTint> {
        @Override
        public SimpleDefaultTint create(Map<String, Object> arguments) {
            Object value = arguments.containsKey("default") ? arguments.getOrDefault("default", 0) : arguments.getOrDefault("value", 0);
            Key type = Key.of(arguments.get("type").toString());
            return new SimpleDefaultTint(type, parseTintValue(value));
        }
    }

    private static class Reader implements TintReader<SimpleDefaultTint> {
        @Override
        public SimpleDefaultTint read(JsonObject json) {
            return new SimpleDefaultTint(Key.of(json.get("type").getAsString()), parseTintValue(json.get("default")));
        }
    }
}
