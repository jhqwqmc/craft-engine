package net.momirealms.craftengine.core.pack.model.definition.tint;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.incendo.cloud.type.Either;

import java.util.List;
import java.util.Map;

public final class ConstantTint implements Tint {
    public static final TintFactory FACTORY = new Factory();
    public static final TintReader READER = new Reader();
    private final Either<Integer, List<Float>> value;

    public ConstantTint(Either<Integer, List<Float>> value) {
        this.value = value;
    }

    public Either<Integer, List<Float>> value() {
        return this.value;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "constant");
        applyAnyTint(json, this.value, "value");
        return json;
    }

    private static class Factory implements TintFactory {
        @Override
        public Tint create(Map<String, Object> arguments) {
            Object value = ResourceConfigUtils.requireNonNullOrThrow(ResourceConfigUtils.get(arguments, "value", "default"), "warning.config.item.model.tint.constant.missing_value");
            return new ConstantTint(parseTintValue(value));
        }
    }

    private static class Reader implements TintReader {
        @Override
        public Tint read(JsonObject json) {
            return new ConstantTint(parseTintValue(json.get("value")));
        }
    }
}
