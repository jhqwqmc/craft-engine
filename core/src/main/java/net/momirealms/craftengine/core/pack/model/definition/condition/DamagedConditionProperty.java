package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class DamagedConditionProperty implements ConditionProperty, LegacyModelPredicate<Boolean> {
    public static final ConditionPropertyFactory FACTORY = new Factory();
    public static final ConditionPropertyReader READER = new Reader();
    public static final DamagedConditionProperty INSTANCE = new DamagedConditionProperty();

    private DamagedConditionProperty() {}

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "damaged");
    }

    @Override
    public String legacyPredicateId(Key material) {
        return "damaged";
    }

    @Override
    public Number toLegacyValue(Boolean value) {
        return value ? 1 : 0;
    }

    private static class Factory implements ConditionPropertyFactory {
        @Override
        public ConditionProperty create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements ConditionPropertyReader {
        @Override
        public ConditionProperty read(JsonObject json) {
            return INSTANCE;
        }
    }
}
