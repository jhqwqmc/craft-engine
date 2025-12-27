package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class ConditionProperties {
    public static final ConditionPropertyType BROKEN = register(Key.of("broken"), BrokenConditionProperty.FACTORY, BrokenConditionProperty.READER);
    public static final ConditionPropertyType BUNDLE_HAS_SELECTED_ITEM = register(Key.of("bundle/has_selected_item"), SimpleConditionProperty.FACTORY, SimpleConditionProperty.READER);
    public static final ConditionPropertyType CARRIED = register(Key.of("carried"), SimpleConditionProperty.FACTORY, SimpleConditionProperty.READER);
    public static final ConditionPropertyType COMPONENT = register(Key.of("component"), ComponentConditionProperty.FACTORY, ComponentConditionProperty.READER);
    public static final ConditionPropertyType DAMAGED = register(Key.of("damaged"), DamagedConditionProperty.FACTORY, DamagedConditionProperty.READER);
    public static final ConditionPropertyType EXTENDED_VIEW = register(Key.of("extended_view"), SimpleConditionProperty.FACTORY, SimpleConditionProperty.READER);
    public static final ConditionPropertyType FISHING_ROD_CAST = register(Key.of("fishing_rod/cast"), RodCastConditionProperty.FACTORY, RodCastConditionProperty.READER);
    public static final ConditionPropertyType HAS_COMPONENT = register(Key.of("has_component"), HasComponentConditionProperty.FACTORY, HasComponentConditionProperty.READER);
    public static final ConditionPropertyType KEYBIND_DOWN = register(Key.of("keybind_down"), KeyBindDownConditionProperty.FACTORY, KeyBindDownConditionProperty.READER);
    public static final ConditionPropertyType SELECTED = register(Key.of("selected"), SimpleConditionProperty.FACTORY, SimpleConditionProperty.READER);
    public static final ConditionPropertyType USING_ITEM = register(Key.of("using_item"), UsingItemConditionProperty.FACTORY, UsingItemConditionProperty.READER);
    public static final ConditionPropertyType VIEW_ENTITY = register(Key.of("view_entity"), SimpleConditionProperty.FACTORY, SimpleConditionProperty.READER);
    public static final ConditionPropertyType CUSTOM_MODEL_DATA = register(Key.of("custom_model_data"), CustomModelDataConditionProperty.FACTORY, CustomModelDataConditionProperty.READER);

    private ConditionProperties() {}

    public static ConditionPropertyType register(Key id, ConditionPropertyFactory factory, ConditionPropertyReader reader) {
        ConditionPropertyType type = new ConditionPropertyType(id, factory, reader);
        ((WritableRegistry<ConditionPropertyType>) BuiltInRegistries.CONDITION_PROPERTY_TYPE)
                .register(ResourceKey.create(Registries.CONDITION_PROPERTY_TYPE.location(), id), type);
        return type;
    }

    public static ConditionProperty fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("property"), "warning.config.item.model.condition.missing_property");
        Key key = Key.withDefaultNamespace(type, "minecraft");
        ConditionPropertyType propertyType = BuiltInRegistries.CONDITION_PROPERTY_TYPE.getValue(key);
        if (propertyType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.condition.invalid_property", type);
        }
        return propertyType.factory().create(map);
    }

    public static ConditionProperty fromJson(JsonObject json) {
        String type = json.get("property").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        ConditionPropertyType propertyType = BuiltInRegistries.CONDITION_PROPERTY_TYPE.getValue(key);
        if (propertyType == null) {
            throw new IllegalArgumentException("Invalid condition property type: " + key);
        }
        return propertyType.reader().read(json);
    }
}
