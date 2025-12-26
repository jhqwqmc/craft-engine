package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class UsingItemConditionProperty implements ConditionProperty, LegacyModelPredicate<Boolean> {
    public static final Key ID = Key.of("minecraft:using_item");
    public static final ConditionPropertyFactory FACTORY = new Factory();
    public static final ConditionPropertyReader READER = new Reader();
    public static final UsingItemConditionProperty INSTANCE = new UsingItemConditionProperty();

    private UsingItemConditionProperty() {}

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", ID.asMinimalString());
    }

    @Override
    public String legacyPredicateId(Key material) {
        if (material.equals(ItemKeys.SHIELD)) return "blocking";
        if (material.equals(ItemKeys.TRIDENT)) return "throwing";
        if (material.equals(ItemKeys.CROSSBOW) || material.equals(ItemKeys.BOW)) return "pulling";
        if (material.equals(ItemKeys.GOAT_HORN)) return "tooting";
        return null;
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
