package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class DamagedConditionProperty implements ConditionProperty, LegacyModelPredicate<Boolean> {
    public static final ConditionPropertyFactory<DamagedConditionProperty> FACTORY = new Factory();
    public static final ConditionPropertyReader<DamagedConditionProperty> READER = new Reader();
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

    private static class Factory implements ConditionPropertyFactory<DamagedConditionProperty> {
        @Override
        public DamagedConditionProperty create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements ConditionPropertyReader<DamagedConditionProperty> {
        @Override
        public DamagedConditionProperty read(JsonObject json) {
            return INSTANCE;
        }
    }
}
