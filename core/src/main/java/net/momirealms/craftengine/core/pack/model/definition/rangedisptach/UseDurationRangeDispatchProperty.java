package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class UseDurationRangeDispatchProperty implements RangeDispatchProperty, LegacyModelPredicate<Number> {
    public static final Key ID = Key.of("minecraft:use_duration");
    public static final RangeDispatchPropertyFactory FACTORY = new Factory();
    public static final RangeDispatchPropertyReader READER = new Reader();
    private final boolean remaining;

    public UseDurationRangeDispatchProperty(boolean remaining) {
        this.remaining = remaining;
    }

    public boolean remaining() {
        return this.remaining;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", ID.asMinimalString());
        if (this.remaining) {
            jsonObject.addProperty("remaining", true);
        }
    }

    @Override
    public String legacyPredicateId(Key material) {
        if (material.equals(ItemKeys.BOW)) return "pull";
        return null;
    }

    @Override
    public Number toLegacyValue(Number value) {
        return value;
    }

    private static class Factory implements RangeDispatchPropertyFactory {
        @Override
        public RangeDispatchProperty create(Map<String, Object> arguments) {
            boolean remaining = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("remaining", false), "remaining");
            return new UseDurationRangeDispatchProperty(remaining);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader {
        @Override
        public RangeDispatchProperty read(JsonObject json) {
            boolean remaining = json.has("remaining") && json.get("remaining").getAsBoolean();
            return new UseDurationRangeDispatchProperty(remaining);
        }
    }
}
