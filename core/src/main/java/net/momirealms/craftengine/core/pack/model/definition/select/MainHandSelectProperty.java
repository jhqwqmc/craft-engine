package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class MainHandSelectProperty implements SelectProperty, LegacyModelPredicate<String> {
    public static final SelectPropertyFactory FACTORY = new Factory();
    public static final SelectPropertyReader READER = new Reader();
    public static final MainHandSelectProperty INSTANCE = new MainHandSelectProperty();

    private MainHandSelectProperty() {}

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "main_hand");
    }

    @Override
    public String legacyPredicateId(Key material) {
        return "lefthanded";
    }

    @Override
    public Number toLegacyValue(String value) {
        if (value.equals("left")) return 1;
        return 0;
    }

    private static class Factory implements SelectPropertyFactory {
        @Override
        public SelectProperty create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements SelectPropertyReader {
        @Override
        public SelectProperty read(JsonObject json) {
            return INSTANCE;
        }
    }
}
