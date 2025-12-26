package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.legacy.LegacyModelPredicate;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.HashMap;
import java.util.Map;

public final class TrimMaterialSelectProperty implements SelectProperty, LegacyModelPredicate<String> {
    public static final Key ID = Key.of("minecraft:trim_material");
    public static final SelectPropertyFactory FACTORY = new Factory();
    public static final SelectPropertyReader READER = new Reader();
    public static final TrimMaterialSelectProperty INSTANCE = new TrimMaterialSelectProperty();
    private static final Map<String, Float> LEGACY_TRIM_DATA = MiscUtils.init(new HashMap<>(), map -> {
        map.put("minecraft:quartz", 0.1f);
        map.put("minecraft:iron", 0.2f);
        map.put("minecraft:netherite", 0.3f);
        map.put("minecraft:redstone", 0.4f);
        map.put("minecraft:copper", 0.5f);
        map.put("minecraft:gold", 0.6f);
        map.put("minecraft:emerald", 0.7f);
        map.put("minecraft:diamond", 0.8f);
        map.put("minecraft:lapis", 0.9f);
        map.put("minecraft:amethyst", 1.0f);
        // INVALID
        map.put("minecraft:resin", 1.1F);
    });

    private TrimMaterialSelectProperty() {}

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", ID.asMinimalString());
    }

    @Override
    public String legacyPredicateId(Key material) {
        return "trim_type";
    }

    @Override
    public Number toLegacyValue(String value) {
        Float f = LEGACY_TRIM_DATA.get(value);
        if (f == null) {
            throw new IllegalArgumentException("Invalid trim material '" + value + "'");
        }
        return f;
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
