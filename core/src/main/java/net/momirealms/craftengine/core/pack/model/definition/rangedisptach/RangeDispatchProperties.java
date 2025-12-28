package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class RangeDispatchProperties {
    public static final RangeDispatchPropertyType<SimpleRangeDispatchProperty> BUNDLE_FULLNESS = register(Key.of("bundle/fullness"), SimpleRangeDispatchProperty.FACTORY, SimpleRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<CompassRangeDispatchProperty> COMPASS = register(Key.of("compass"), CompassRangeDispatchProperty.FACTORY, CompassRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<SimpleRangeDispatchProperty> COOLDOWN = register(Key.of("cooldown"), SimpleRangeDispatchProperty.FACTORY, SimpleRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<NormalizeRangeDispatchProperty> COUNT = register(Key.of("count"), NormalizeRangeDispatchProperty.FACTORY, NormalizeRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<CrossBowPullingRangeDispatchProperty> CROSSBOW_PULL = register(Key.of("crossbow/pull"), CrossBowPullingRangeDispatchProperty.FACTORY, CrossBowPullingRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<DamageRangeDispatchProperty> DAMAGE = register(Key.of("damage"), DamageRangeDispatchProperty.FACTORY, DamageRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<TimeRangeDispatchProperty> TIME = register(Key.of("time"), TimeRangeDispatchProperty.FACTORY, TimeRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<UseCycleRangeDispatchProperty> USE_CYCLE = register(Key.of("use_cycle"), UseCycleRangeDispatchProperty.FACTORY, UseCycleRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<UseDurationRangeDispatchProperty> USE_DURATION = register(Key.of("use_duration"), UseDurationRangeDispatchProperty.FACTORY, UseDurationRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType<CustomModelDataRangeDispatchProperty> CUSTOM_MODEL_DATA = register(Key.of("custom_model_data"), CustomModelDataRangeDispatchProperty.FACTORY, CustomModelDataRangeDispatchProperty.READER);

    private RangeDispatchProperties() {}

    public static <T extends RangeDispatchProperty> RangeDispatchPropertyType<T> register(Key id, RangeDispatchPropertyFactory<T> factory, RangeDispatchPropertyReader<T> reader) {
        RangeDispatchPropertyType<T> type = new RangeDispatchPropertyType<>(id, factory, reader);
        ((WritableRegistry<RangeDispatchPropertyType<? extends RangeDispatchProperty>>) BuiltInRegistries.RANGE_DISPATCH_PROPERTY_TYPE)
                .register(ResourceKey.create(Registries.RANGE_DISPATCH_PROPERTY_TYPE.location(), id), type);
        return type;
    }

    public static RangeDispatchProperty fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("property"), "warning.config.item.model.range_dispatch.missing_property");
        Key key = Key.withDefaultNamespace(type, "minecraft");
        RangeDispatchPropertyType<? extends RangeDispatchProperty> propertyType = BuiltInRegistries.RANGE_DISPATCH_PROPERTY_TYPE.getValue(key);
        if (propertyType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.range_dispatch.invalid_property", type);
        }
        return propertyType.factory().create(map);
    }

    public static RangeDispatchProperty fromJson(JsonObject json) {
        String type = json.get("property").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        RangeDispatchPropertyType<? extends RangeDispatchProperty> propertyType = BuiltInRegistries.RANGE_DISPATCH_PROPERTY_TYPE.getValue(key);
        if (propertyType == null) {
            throw new IllegalArgumentException("Invalid range dispatch property type: " + key);
        }
        return propertyType.reader().read(json);
    }
}
