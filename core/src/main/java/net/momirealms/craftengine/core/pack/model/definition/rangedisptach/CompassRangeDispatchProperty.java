package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class CompassRangeDispatchProperty implements RangeDispatchProperty {
    public static final Key ID = Key.of("minecraft:compass");
    public static final RangeDispatchPropertyFactory FACTORY = new Factory();
    public static final RangeDispatchPropertyReader READER = new Reader();
    private final String target;
    private final boolean wobble;

    public CompassRangeDispatchProperty(String target, boolean wobble) {
        this.target = target;
        this.wobble = wobble;
    }

    public String target() {
        return this.target;
    }

    public boolean wobble() {
        return this.wobble;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", ID.asMinimalString());
        jsonObject.addProperty("target", this.target);
        if (!this.wobble) {
            jsonObject.addProperty("wobble", false);
        }
    }

    private static class Factory implements RangeDispatchPropertyFactory {
        @Override
        public RangeDispatchProperty create(Map<String, Object> arguments) {
            String targetObj = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("target"), "warning.config.item.model.range_dispatch.compass.missing_target");
            boolean wobble = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("wobble", true), "wobble");
            return new CompassRangeDispatchProperty(targetObj, wobble);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader {
        @Override
        public RangeDispatchProperty read(JsonObject json) {
            String target = json.get("target").getAsString();
            boolean wobble = !json.has("wobble") || json.get("wobble").getAsBoolean();
            return new CompassRangeDispatchProperty(target, wobble);
        }
    }
}
