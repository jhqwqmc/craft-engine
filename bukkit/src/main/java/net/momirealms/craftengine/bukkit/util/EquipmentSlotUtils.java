package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.attribute.EquipmentSetSlot;
import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EquipmentSlotProxy;

public final class EquipmentSlotUtils {
    private EquipmentSlotUtils() {}

    public static Object toNMSEquipmentSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case MAINHAND -> EquipmentSlotProxy.MAINHAND;
            case OFFHAND -> EquipmentSlotProxy.OFFHAND;
            case FEET -> EquipmentSlotProxy.FEET;
            case LEGS -> EquipmentSlotProxy.LEGS;
            case CHEST -> EquipmentSlotProxy.CHEST;
            case HEAD -> EquipmentSlotProxy.HEAD;
            case BODY -> EquipmentSlotProxy.BODY;
            case SADDLE -> EquipmentSlotProxy.SADDLE;
        };
    }

    public static EquipmentSlot fromNMSEquipmentSlot(Object equipmentSlot) {
        Enum<?> directionEnum = (Enum<?>) equipmentSlot;
        int index = directionEnum.ordinal();
        return EquipmentSlot.values()[index];
    }

    public static EquipmentSetSlot toEquipmentSetSlot(org.bukkit.inventory.EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HAND -> EquipmentSetSlot.MAINHAND;
            case OFF_HAND -> EquipmentSetSlot.OFFHAND;
            case FEET -> EquipmentSetSlot.FEET;
            case LEGS -> EquipmentSetSlot.LEGS;
            case CHEST -> EquipmentSetSlot.CHEST;
            case HEAD -> EquipmentSetSlot.HEAD;
            case BODY -> EquipmentSetSlot.BODY;
            case SADDLE -> EquipmentSetSlot.SADDLE;
        };
    }
}
