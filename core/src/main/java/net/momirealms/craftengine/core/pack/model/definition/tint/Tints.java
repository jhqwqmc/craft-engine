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
    public static final TintType<ConstantTint> CONSTANT = register(Key.of("constant"), ConstantTint.FACTORY, ConstantTint.READER);
    public static final TintType<CustomModelDataTint> CUSTOM_MODEL_DATA = register(Key.of("custom_model_data"), CustomModelDataTint.FACTORY, CustomModelDataTint.READER);
    public static final TintType<SimpleDefaultTint> DYE = register(Key.of("dye"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType<SimpleDefaultTint> FIREWORK = register(Key.of("firework"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType<SimpleDefaultTint> MAP_COLOR = register(Key.of("map_color"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType<SimpleDefaultTint> POTION = register(Key.of("potion"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType<SimpleDefaultTint> TEAM = register(Key.of("team"), SimpleDefaultTint.FACTORY, SimpleDefaultTint.READER);
    public static final TintType<GrassTint> GRASS = register(Key.of("grass"), GrassTint.FACTORY, GrassTint.READER);

    private Tints() {}

    public static <T extends Tint> TintType<T> register(Key id, TintFactory<T> factory, TintReader<T> reader) {
        TintType<T> type = new TintType<>(id, factory, reader);
        ((WritableRegistry<TintType<? extends Tint>>) BuiltInRegistries.TINT_TYPE)
                .register(ResourceKey.create(Registries.TINT_TYPE.location(), id), type);
        return type;
    }

    public static Tint fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.item.model.tint.missing_type");
        Key key = Key.withDefaultNamespace(type, "minecraft");
        TintType<? extends Tint> tintType = BuiltInRegistries.TINT_TYPE.getValue(key);
        if (tintType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.tint.invalid_type", type);
        }
        return tintType.factory().create(map);
    }

    public static Tint fromJson(JsonObject json) {
        String type = json.get("type").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        TintType<? extends Tint> tintType = BuiltInRegistries.TINT_TYPE.getValue(key);
        if (tintType == null) {
            throw new IllegalArgumentException("Invalid tint type: " + type);
        }
        return tintType.reader().read(json);
    }
}
