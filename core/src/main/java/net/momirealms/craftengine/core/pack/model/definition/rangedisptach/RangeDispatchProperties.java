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
    public static final RangeDispatchPropertyType BUNDLE_FULLNESS = register(Key.of("minecraft:bundle/fullness"), SimpleRangeDispatchProperty.FACTORY, SimpleRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType COMPASS = register(CompassRangeDispatchProperty.ID, CompassRangeDispatchProperty.FACTORY, CompassRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType COOLDOWN = register(Key.of("minecraft:cooldown"), SimpleRangeDispatchProperty.FACTORY, SimpleRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType COUNT = register(Key.of("minecraft:count"), NormalizeRangeDispatchProperty.FACTORY, NormalizeRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType CROSSBOW_PULL = register(CrossBowPullingRangeDispatchProperty.ID, CrossBowPullingRangeDispatchProperty.FACTORY, CrossBowPullingRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType DAMAGE = register(DamageRangeDispatchProperty.ID, DamageRangeDispatchProperty.FACTORY, DamageRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType TIME = register(TimeRangeDispatchProperty.ID, TimeRangeDispatchProperty.FACTORY, TimeRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType USE_CYCLE = register(UseCycleRangeDispatchProperty.ID, UseCycleRangeDispatchProperty.FACTORY, UseCycleRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType USE_DURATION = register(UseDurationRangeDispatchProperty.ID, UseDurationRangeDispatchProperty.FACTORY, UseDurationRangeDispatchProperty.READER);
    public static final RangeDispatchPropertyType CUSTOM_MODEL_DATA = register(CustomModelDataRangeDispatchProperty.ID, CustomModelDataRangeDispatchProperty.FACTORY, CustomModelDataRangeDispatchProperty.READER);

    private RangeDispatchProperties() {}

    public static RangeDispatchPropertyType register(Key id, RangeDispatchPropertyFactory factory, RangeDispatchPropertyReader reader) {
        RangeDispatchPropertyType type = new RangeDispatchPropertyType(id, factory, reader);
        ((WritableRegistry<RangeDispatchPropertyType>) BuiltInRegistries.RANGE_DISPATCH_PROPERTY_TYPE)
                .register(ResourceKey.create(Registries.RANGE_DISPATCH_PROPERTY_TYPE.location(), id), type);
        return type;
    }

    public static RangeDispatchProperty fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("property"), "warning.config.item.model.range_dispatch.missing_property");
        Key key = Key.withDefaultNamespace(type, "minecraft");
        RangeDispatchPropertyType propertyType = BuiltInRegistries.RANGE_DISPATCH_PROPERTY_TYPE.getValue(key);
        if (propertyType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.range_dispatch.invalid_property", type);
        }
        return propertyType.factory().create(map);
    }

    public static RangeDispatchProperty fromJson(JsonObject json) {
        String type = json.get("property").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        RangeDispatchPropertyType propertyType = BuiltInRegistries.RANGE_DISPATCH_PROPERTY_TYPE.getValue(key);
        if (propertyType == null) {
            throw new IllegalArgumentException("Invalid range dispatch property type: " + key);
        }
        return propertyType.reader().read(json);
    }
}
