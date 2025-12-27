package net.momirealms.craftengine.core.entity.furniture.element;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;
import java.util.Optional;

public class FurnitureElementConfigs {
    protected FurnitureElementConfigs() {}

    public static <E extends FurnitureElement> FurnitureElementConfigType<E> register(Key key, FurnitureElementConfigFactory<E> factory) {
        FurnitureElementConfigType<E> type = new FurnitureElementConfigType<>(key, factory);
        ((WritableRegistry<FurnitureElementConfigType<?>>) BuiltInRegistries.FURNITURE_ELEMENT_TYPE)
                .register(ResourceKey.create(Registries.FURNITURE_ELEMENT_TYPE.location(), key), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <E extends FurnitureElement> FurnitureElementConfig<E> fromMap(Map<String, Object> arguments) {
        Key type = guessType(arguments);
        FurnitureElementConfigType<E> configType = (FurnitureElementConfigType<E>) BuiltInRegistries.FURNITURE_ELEMENT_TYPE.getValue(type);
        if (configType == null) {
            throw new LocalizedResourceConfigException("warning.config.furniture.element.invalid_type", type.toString());
        }
        return configType.factory().create(arguments);
    }

    private static Key guessType(Map<String, Object> arguments) {
        return Key.ce(Optional.ofNullable(arguments.get("type")).map(String::valueOf).orElseGet(() -> {
            if (arguments.containsKey("text")) {
                return "text_display";
            } else {
                return "item_display";
            }
        }));
    }
}
