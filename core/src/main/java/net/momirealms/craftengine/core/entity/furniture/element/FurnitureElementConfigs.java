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
    public static final Key ITEM_DISPLAY = Key.of("craftengine:item_display");
    public static final Key TEXT_DISPLAY = Key.of("craftengine:text_display");
    public static final Key ITEM = Key.of("craftengine:item");

    public static void register(Key key, FurnitureElementConfigFactory<?> type) {
        ((WritableRegistry<FurnitureElementConfigFactory<?>>) BuiltInRegistries.FURNITURE_ELEMENT_TYPE)
                .register(ResourceKey.create(Registries.FURNITURE_ELEMENT_TYPE.location(), key), type);
    }

    public static <E extends FurnitureElement> FurnitureElementConfig<E> fromMap(Map<String, Object> arguments) {
        Key type = Optional.ofNullable(arguments.get("type")).map(String::valueOf).map(it -> Key.withDefaultNamespace(it, "craftengine")).orElse(ITEM_DISPLAY);
        @SuppressWarnings("unchecked")
        FurnitureElementConfigFactory<E> factory = (FurnitureElementConfigFactory<E>) BuiltInRegistries.FURNITURE_ELEMENT_TYPE.getValue(type);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.furniture.element.invalid_type", type.toString());
        }
        return factory.create(arguments);
    }
}
