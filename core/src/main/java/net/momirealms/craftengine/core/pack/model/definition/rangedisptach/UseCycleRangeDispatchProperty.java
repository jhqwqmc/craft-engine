package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class UseCycleRangeDispatchProperty implements RangeDispatchProperty {
    public static final Key ID = Key.of("minecraft:use_cycle");
    public static final RangeDispatchPropertyFactory FACTORY = new Factory();
    public static final RangeDispatchPropertyReader READER = new Reader();
    private final float period;

    public UseCycleRangeDispatchProperty(float period) {
        this.period = period;
    }

    public float period() {
        return this.period;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", ID.asMinimalString());
        jsonObject.addProperty("period", this.period);
    }

    private static class Factory implements RangeDispatchPropertyFactory {
        @Override
        public RangeDispatchProperty create(Map<String, Object> arguments) {
            float period = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("period", 0), "period");
            return new UseCycleRangeDispatchProperty(period);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader {
        @Override
        public RangeDispatchProperty read(JsonObject json) {
            float period = json.has("period") ? json.get("period").getAsFloat() : 1.0f;
            return new UseCycleRangeDispatchProperty(period);
        }
    }
}
