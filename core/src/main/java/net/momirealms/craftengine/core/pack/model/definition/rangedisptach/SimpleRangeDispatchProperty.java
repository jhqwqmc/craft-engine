package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class SimpleRangeDispatchProperty implements RangeDispatchProperty {
    public static final RangeDispatchPropertyFactory<SimpleRangeDispatchProperty> FACTORY = new Factory();
    public static final RangeDispatchPropertyReader<SimpleRangeDispatchProperty> READER = new Reader();
    private final Key type;

    public SimpleRangeDispatchProperty(Key type) {
        this.type = type;
    }

    public Key type() {
        return this.type;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", this.type.asMinimalString());
    }

    private static class Factory implements RangeDispatchPropertyFactory<SimpleRangeDispatchProperty> {
        @Override
        public SimpleRangeDispatchProperty create(Map<String, Object> arguments) {
            Key type = Key.of(arguments.get("property").toString());
            return new SimpleRangeDispatchProperty(type);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader<SimpleRangeDispatchProperty> {
        @Override
        public SimpleRangeDispatchProperty read(JsonObject json) {
            Key type = Key.of(json.get("property").getAsString());
            return new SimpleRangeDispatchProperty(type);
        }
    }
}
