package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class UseCycleRangeDispatchProperty implements RangeDispatchProperty {
    public static final RangeDispatchPropertyFactory<UseCycleRangeDispatchProperty> FACTORY = new Factory();
    public static final RangeDispatchPropertyReader<UseCycleRangeDispatchProperty> READER = new Reader();
    private final float period;

    public UseCycleRangeDispatchProperty(float period) {
        this.period = period;
    }

    public float period() {
        return this.period;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "use_cycle");
        jsonObject.addProperty("period", this.period);
    }

    private static class Factory implements RangeDispatchPropertyFactory<UseCycleRangeDispatchProperty> {
        @Override
        public UseCycleRangeDispatchProperty create(Map<String, Object> arguments) {
            float period = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("period", 0), "period");
            return new UseCycleRangeDispatchProperty(period);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader<UseCycleRangeDispatchProperty> {
        @Override
        public UseCycleRangeDispatchProperty read(JsonObject json) {
            float period = json.has("period") ? json.get("period").getAsFloat() : 1.0f;
            return new UseCycleRangeDispatchProperty(period);
        }
    }
}
