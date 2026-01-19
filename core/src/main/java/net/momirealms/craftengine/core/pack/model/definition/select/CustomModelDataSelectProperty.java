package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class CustomModelDataSelectProperty implements SelectProperty {
    public static final SelectPropertyFactory<CustomModelDataSelectProperty> FACTORY = new Factory();
    public static final SelectPropertyReader<CustomModelDataSelectProperty> READER = new Reader();
    private final int index;

    public CustomModelDataSelectProperty(int index) {
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "custom_model_data");
        jsonObject.addProperty("index", this.index);
    }

    private static class Factory implements SelectPropertyFactory<CustomModelDataSelectProperty> {
        @Override
        public CustomModelDataSelectProperty create(Map<String, Object> arguments) {
            int index = ResourceConfigUtils.getAsInt(arguments.getOrDefault("index", 0), "index");
            return new CustomModelDataSelectProperty(index);
        }
    }

    private static class Reader implements SelectPropertyReader<CustomModelDataSelectProperty> {
        @Override
        public CustomModelDataSelectProperty read(JsonObject json) {
            int index = json.has("index") ? json.get("index").getAsInt() : 0;
            return new CustomModelDataSelectProperty(index);
        }
    }
}
