package net.momirealms.craftengine.core.attribute;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class EquipmentSetSlot {
    private static final Map<String, EquipmentSetSlot> BY_NAME = new HashMap<>();
    private final String name;

    private EquipmentSetSlot(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public static EquipmentSetSlot of(String name) {
        EquipmentSetSlot equipmentSetSlot = new EquipmentSetSlot(name);
        if (BY_NAME.containsKey(name)) throw new IllegalArgumentException("Slot with name " + name + " already exists");
        BY_NAME.put(name, equipmentSetSlot);
        return equipmentSetSlot;
    }

    @Nullable
    public static EquipmentSetSlot byName(String name) {
        return BY_NAME.get(name);
    }
}
