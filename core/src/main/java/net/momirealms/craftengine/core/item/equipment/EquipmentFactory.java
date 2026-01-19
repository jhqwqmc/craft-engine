package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public interface EquipmentFactory<E extends Equipment> {

    E create(Key id, Map<String, Object> args);
}
