package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class CustomModelDataConditionProperty implements ConditionProperty {
    public static final ConditionPropertyFactory FACTORY = new Factory();
    public static final ConditionPropertyReader READER = new Reader();
    private final int index;

    public CustomModelDataConditionProperty(int index) {
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "custom_model_data");
        if (this.index != 0)
            jsonObject.addProperty("index", this.index);
    }

    private static class Factory implements ConditionPropertyFactory {
        @Override
        public ConditionProperty create(Map<String, Object> arguments) {
            int index = ResourceConfigUtils.getAsInt(arguments.getOrDefault("index", 0), "index");
            return new CustomModelDataConditionProperty(index);
        }
    }

    private static class Reader implements ConditionPropertyReader {
        @Override
        public ConditionProperty read(JsonObject json) {
            int index = json.has("index") ? json.get("index").getAsInt() : 0;
            return new CustomModelDataConditionProperty(index);
        }
    }
}
