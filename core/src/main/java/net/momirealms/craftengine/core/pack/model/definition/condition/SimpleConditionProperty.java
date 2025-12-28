package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class SimpleConditionProperty implements ConditionProperty {
    public static final ConditionPropertyFactory<SimpleConditionProperty> FACTORY = new Factory();
    public static final ConditionPropertyReader<SimpleConditionProperty> READER = new Reader();
    private final Key type;

    public SimpleConditionProperty(Key type) {
        this.type = type;
    }

    public Key type() {
        return type;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", this.type.asMinimalString());
    }

    private static class Factory implements ConditionPropertyFactory<SimpleConditionProperty> {
        @Override
        public SimpleConditionProperty create(Map<String, Object> arguments) {
            Key type = Key.of(arguments.get("property").toString());
            return new SimpleConditionProperty(type);
        }
    }

    private static class Reader implements ConditionPropertyReader<SimpleConditionProperty> {
        @Override
        public SimpleConditionProperty read(JsonObject json) {
            return new SimpleConditionProperty(Key.of(json.get("property").getAsString()));
        }
    }
}
