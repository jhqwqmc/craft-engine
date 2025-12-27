package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class SelectProperties {
    public static final SelectPropertyType BLOCK_STATE = register(Key.of("block_state"), BlockStateSelectProperty.FACTORY, BlockStateSelectProperty.READER);
    public static final SelectPropertyType CHARGE_TYPE = register(Key.of("charge_type"), ChargeTypeSelectProperty.FACTORY, ChargeTypeSelectProperty.READER);
    public static final SelectPropertyType COMPONENT = register(Key.of("component"), ComponentSelectProperty.FACTORY, ComponentSelectProperty.READER);
    public static final SelectPropertyType CONTEXT_DIMENSION = register(Key.of("context_dimension"), SimpleSelectProperty.FACTORY, SimpleSelectProperty.READER);
    public static final SelectPropertyType CONTEXT_ENTITY_TYPE = register(Key.of("context_entity_type"), SimpleSelectProperty.FACTORY, SimpleSelectProperty.READER);
    public static final SelectPropertyType DISPLAY_CONTEXT = register(Key.of("display_context"), DisplayContextSelectProperty.FACTORY, DisplayContextSelectProperty.READER);
    public static final SelectPropertyType LOCAL_TIME = register(Key.of("local_time"), LocalTimeSelectProperty.FACTORY, LocalTimeSelectProperty.READER);
    public static final SelectPropertyType MAIN_HAND = register(Key.of("main_hand"), MainHandSelectProperty.FACTORY, MainHandSelectProperty.READER);
    public static final SelectPropertyType TRIM_MATERIAL = register(Key.of("trim_material"), TrimMaterialSelectProperty.FACTORY, TrimMaterialSelectProperty.READER);
    public static final SelectPropertyType CUSTOM_MODEL_DATA = register(Key.of("custom_model_data"), CustomModelDataSelectProperty.FACTORY, CustomModelDataSelectProperty.READER);

    private SelectProperties() {}

    public static SelectPropertyType register(Key id, SelectPropertyFactory factory, SelectPropertyReader reader) {
        SelectPropertyType type = new SelectPropertyType(id, factory, reader);
        ((WritableRegistry<SelectPropertyType>) BuiltInRegistries.SELECT_PROPERTY_TYPE)
                .register(ResourceKey.create(Registries.SELECT_PROPERTY_TYPE.location(), id), type);
        return type;
    }

    public static SelectProperty fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("property"), "warning.config.item.model.select.missing_property");
        Key key = Key.withDefaultNamespace(type, "minecraft");
        SelectPropertyType selectPropertyType = BuiltInRegistries.SELECT_PROPERTY_TYPE.getValue(key);
        if (selectPropertyType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.select.invalid_property", type);
        }
        return selectPropertyType.factory().create(map);
    }

    public static SelectProperty fromJson(JsonObject json) {
        String type = json.get("property").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        SelectPropertyType selectPropertyType = BuiltInRegistries.SELECT_PROPERTY_TYPE.getValue(key);
        if (selectPropertyType == null) {
            throw new IllegalArgumentException("Invalid select property type: " + key);
        }
        return selectPropertyType.reader().read(json);
    }
}
