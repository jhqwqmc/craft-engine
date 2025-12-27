package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class Equipments {
    public static final EquipmentType<TrimBasedEquipment> TRIM = register(Key.ce("trim"), TrimBasedEquipment.FACTORY);
    public static final EquipmentType<ComponentBasedEquipment> COMPONENT = register(Key.ce("component"), ComponentBasedEquipment.FACTORY);

    private Equipments() {}

    public static <E extends Equipment> EquipmentType<E> register(Key key, EquipmentFactory<E> factory) {
        EquipmentType<E> type = new EquipmentType<>(key, factory);
        ((WritableRegistry<EquipmentType<?>>) BuiltInRegistries.EQUIPMENT_TYPE)
                .register(ResourceKey.create(Registries.EQUIPMENT_TYPE.location(), key), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Equipment> E fromMap(Key id, Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.equipment.missing_type");
        Key key = Key.ce(type);
        EquipmentType<E> equipmentType = (EquipmentType<E>) BuiltInRegistries.EQUIPMENT_TYPE.getValue(key);
        if (equipmentType == null) {
            throw new LocalizedResourceConfigException("warning.config.equipment.invalid_type", type);
        }
        return equipmentType.factory().create(id, map);
    }
}
