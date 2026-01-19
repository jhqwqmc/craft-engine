package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class CustomModelDataRangeDispatchProperty implements RangeDispatchProperty, LegacyModelPredicate<Number> {
    public static final RangeDispatchPropertyFactory<CustomModelDataRangeDispatchProperty> FACTORY = new Factory();
    public static final RangeDispatchPropertyReader<CustomModelDataRangeDispatchProperty> READER = new Reader();
    private final int index;

    public CustomModelDataRangeDispatchProperty(int index) {
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

    @Override
    public String legacyPredicateId(Key material) {
        return "custom_model_data";
    }

    @Override
    public Number toLegacyValue(Number value) {
        return value.intValue();
    }

    private static class Factory implements RangeDispatchPropertyFactory<CustomModelDataRangeDispatchProperty> {
        @Override
        public CustomModelDataRangeDispatchProperty create(Map<String, Object> arguments) {
            int index = ResourceConfigUtils.getAsInt(arguments.getOrDefault("index", 0), "index");
            return new CustomModelDataRangeDispatchProperty(index);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader<CustomModelDataRangeDispatchProperty> {
        @Override
        public CustomModelDataRangeDispatchProperty read(JsonObject json) {
            int index = json.has("index") ? json.get("index").getAsInt() : 0;
            return new CustomModelDataRangeDispatchProperty(index);
        }
    }
}
