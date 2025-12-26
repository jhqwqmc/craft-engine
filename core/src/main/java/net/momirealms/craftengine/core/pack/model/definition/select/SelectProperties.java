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
    public static final SelectPropertyType BLOCK_STATE = register(BlockStateSelectProperty.ID, BlockStateSelectProperty.FACTORY, BlockStateSelectProperty.READER);
    public static final SelectPropertyType CHARGE_TYPE = register(ChargeTypeSelectProperty.ID, ChargeTypeSelectProperty.FACTORY, ChargeTypeSelectProperty.READER);
    public static final SelectPropertyType COMPONENT = register(ComponentSelectProperty.ID, ComponentSelectProperty.FACTORY, ComponentSelectProperty.READER);
    public static final SelectPropertyType CONTEXT_DIMENSION = register(Key.of("minecraft:context_dimension"), SimpleSelectProperty.FACTORY, SimpleSelectProperty.READER);
    public static final SelectPropertyType CONTEXT_ENTITY_TYPE = register(Key.of("minecraft:context_entity_type"), SimpleSelectProperty.FACTORY, SimpleSelectProperty.READER);
    public static final SelectPropertyType DISPLAY_CONTEXT = register(DisplayContextSelectProperty.ID, DisplayContextSelectProperty.FACTORY, DisplayContextSelectProperty.READER);
    public static final SelectPropertyType LOCAL_TIME = register(LocalTimeSelectProperty.ID, LocalTimeSelectProperty.FACTORY, LocalTimeSelectProperty.READER);
    public static final SelectPropertyType MAIN_HAND = register(MainHandSelectProperty.ID, MainHandSelectProperty.FACTORY, MainHandSelectProperty.READER);
    public static final SelectPropertyType TRIM_MATERIAL = register(TrimMaterialSelectProperty.ID, TrimMaterialSelectProperty.FACTORY, TrimMaterialSelectProperty.READER);
    public static final SelectPropertyType CUSTOM_MODEL_DATA = register(CustomModelDataSelectProperty.ID, CustomModelDataSelectProperty.FACTORY, CustomModelDataSelectProperty.READER);

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
