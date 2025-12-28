package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class BrokenConditionProperty implements ConditionProperty, LegacyModelPredicate<Boolean> {
    public static final ConditionPropertyFactory<BrokenConditionProperty> FACTORY = new Factory();
    public static final ConditionPropertyReader<BrokenConditionProperty> READER = new Reader();
    public static final BrokenConditionProperty INSTANCE = new BrokenConditionProperty();

    private BrokenConditionProperty() {}

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "broken");
    }

    @Override
    public String legacyPredicateId(Key material) {
        if (material.equals(ItemKeys.ELYTRA)) return "broken";
        return null;
    }

    @Override
    public Number toLegacyValue(Boolean value) {
        return value ? 1 : 0;
    }

    private static class Factory implements ConditionPropertyFactory<BrokenConditionProperty> {
        @Override
        public BrokenConditionProperty create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements ConditionPropertyReader<BrokenConditionProperty> {
        @Override
        public BrokenConditionProperty read(JsonObject json) {
            return INSTANCE;
        }
    }
}
