package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class SimpleSelectProperty implements SelectProperty {
    public static final SelectPropertyFactory<SimpleSelectProperty> FACTORY = new Factory();
    public static final SelectPropertyReader<SimpleSelectProperty> READER = new Reader();
    private final Key type;

    public SimpleSelectProperty(Key type) {
        this.type = type;
    }

    public Key type() {
        return this.type;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", this.type.asMinimalString());
    }

    private static class Factory implements SelectPropertyFactory<SimpleSelectProperty> {
        @Override
        public SimpleSelectProperty create(Map<String, Object> arguments) {
            Key type = Key.of(arguments.get("property").toString());
            return new SimpleSelectProperty(type);
        }
    }

    private static class Reader implements SelectPropertyReader<SimpleSelectProperty> {
        @Override
        public SimpleSelectProperty read(JsonObject json) {
            Key type = Key.of(json.get("property").getAsString());
            return new SimpleSelectProperty(type);
        }
    }
}
