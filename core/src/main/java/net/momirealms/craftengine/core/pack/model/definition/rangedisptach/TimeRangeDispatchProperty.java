package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class TimeRangeDispatchProperty implements RangeDispatchProperty {
    public static final RangeDispatchPropertyFactory FACTORY = new Factory();
    public static final RangeDispatchPropertyReader READER = new Reader();
    private final String source;
    private final boolean wobble;

    public TimeRangeDispatchProperty(String source, boolean wobble) {
        this.source = source;
        this.wobble = wobble;
    }

    public String source() {
        return this.source;
    }

    public boolean wobble() {
        return this.wobble;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "time");
        jsonObject.addProperty("source", this.source);
        if (!this.wobble) {
            jsonObject.addProperty("wobble", false);
        }
    }

    private static class Factory implements RangeDispatchPropertyFactory {
        @Override
        public RangeDispatchProperty create(Map<String, Object> arguments) {
            String sourceObj = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("source"), "warning.config.item.model.range_dispatch.time.missing_source");
            boolean wobble = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("wobble", true), "wobble");
            return new TimeRangeDispatchProperty(sourceObj, wobble);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader {
        @Override
        public RangeDispatchProperty read(JsonObject json) {
            String source = json.get("source").getAsString();
            boolean wobble = !json.has("wobble") || json.get("wobble").getAsBoolean();
            return new TimeRangeDispatchProperty(source, wobble);
        }
    }
}
