package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class EquipmentSetSlot {
    private static final Map<String, EquipmentSetSlot> BY_NAME = new HashMap<>();
    public static final EquipmentSetSlot MAINHAND = EquipmentSetSlot.create("mainhand");
    public static final EquipmentSetSlot OFFHAND = EquipmentSetSlot.create("offhand");
    public static final EquipmentSetSlot HEAD = EquipmentSetSlot.create("head");
    public static final EquipmentSetSlot CHEST = EquipmentSetSlot.create("chest");
    public static final EquipmentSetSlot LEGS = EquipmentSetSlot.create("legs");
    public static final EquipmentSetSlot FEET = EquipmentSetSlot.create("feet");
    public static final EquipmentSetSlot BODY = EquipmentSetSlot.create("body");
    public static final EquipmentSetSlot SADDLE = EquipmentSetSlot.create("saddle");
    private final String name;

    private EquipmentSetSlot(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public static EquipmentSetSlot create(String name) {
        EquipmentSetSlot equipmentSetSlot = new EquipmentSetSlot(name);
        if (BY_NAME.containsKey(name)) throw new IllegalArgumentException("Slot with name " + name + " already exists");
        BY_NAME.put(name, equipmentSetSlot);
        return equipmentSetSlot;
    }

    @Nullable
    public static EquipmentSetSlot byName(String name) {
        return BY_NAME.get(name);
    }

    public static EquipmentSetSlot fromEquipmentSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case MAINHAND -> MAINHAND;
            case OFFHAND -> OFFHAND;
            case FEET -> FEET;
            case LEGS -> LEGS;
            case CHEST -> CHEST;
            case HEAD -> HEAD;
            case BODY -> BODY;
            case SADDLE -> SADDLE;
        };
    }
}
