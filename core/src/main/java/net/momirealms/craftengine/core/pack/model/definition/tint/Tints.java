package net.momirealms.craftengine.core.pack.model.definition.tint;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class Tints {
    public static final TintType CONSTANT = register(ConstantTint.ID, ConstantTint.FACTORY, ConstantTint.READER);
    public static final TintType CUSTOM_MODEL_DATA = register(CustomModelDataTint.ID, CustomModelDataTint.FACTORY, CustomModelDataTint.READER);
    public static final TintType DYE = register(Key.of("minecraft:dye"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType FIREWORK = register(Key.of("minecraft:firework"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType MAP_COLOR = register(Key.of("minecraft:map_color"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType POTION = register(Key.of("minecraft:potion"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType TEAM = register(Key.of("minecraft:team"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType GRASS = register(GrassTint.ID, GrassTint.FACTORY, GrassTint.READER);

    private Tints() {}

    public static TintType register(Key id, TintFactory factory, TintReader reader) {
        TintType type = new TintType(id, factory, reader);
        ((WritableRegistry<TintType>) BuiltInRegistries.TINT_TYPE)
                .register(ResourceKey.create(Registries.TINT_TYPE.location(), id), type);
        return type;
    }

    public static Tint fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.item.model.tint.missing_type");
        Key key = Key.withDefaultNamespace(type, "minecraft");
        TintType tintType = BuiltInRegistries.TINT_TYPE.getValue(key);
        if (tintType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.tint.invalid_type", type);
        }
        return tintType.factory().create(map);
    }

    public static Tint fromJson(JsonObject json) {
        String type = json.get("type").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        TintType tintType = BuiltInRegistries.TINT_TYPE.getValue(key);
        if (tintType == null) {
            throw new IllegalArgumentException("Invalid tint type: " + type);
        }
        return tintType.reader().read(json);
    }
}
