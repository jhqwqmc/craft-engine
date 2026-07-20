package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.EquipmentSlot;

public final class EquipmentSetSlots {
    public static final EquipmentSetSlot MAINHAND = EquipmentSetSlot.of("mainhand");
    public static final EquipmentSetSlot OFFHAND = EquipmentSetSlot.of("offhand");
    public static final EquipmentSetSlot HEAD = EquipmentSetSlot.of("head");
    public static final EquipmentSetSlot CHEST = EquipmentSetSlot.of("chest");
    public static final EquipmentSetSlot LEGS = EquipmentSetSlot.of("legs");
    public static final EquipmentSetSlot FEET = EquipmentSetSlot.of("feet");

    private EquipmentSetSlots() {
    }

    public static EquipmentSetSlot fromEquipmentSlot(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot) {
            case MAINHAND -> {
                return MAINHAND;
            }
            case OFFHAND -> {
                return OFFHAND;
            }
            case HEAD -> {
                return HEAD;
            }
            case CHEST -> {
                return CHEST;
            }
            case LEGS -> {
                return LEGS;
            }
            case FEET -> {
                return FEET;
            }
            default -> {
                return null;
            }
        }
    }
}
