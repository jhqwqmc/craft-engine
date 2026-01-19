package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.core.entity.EquipmentSlot;

public final class EquipmentSlotUtils {
    private EquipmentSlotUtils() {}

    public static Object toNMSEquipmentSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case MAINHAND -> CoreReflections.instance$EquipmentSlot$MAINHAND;
            case OFFHAND -> CoreReflections.instance$EquipmentSlot$OFFHAND;
            case FEET -> CoreReflections.instance$EquipmentSlot$FEET;
            case LEGS -> CoreReflections.instance$EquipmentSlot$LEGS;
            case CHEST -> CoreReflections.instance$EquipmentSlot$CHEST;
            case HEAD -> CoreReflections.instance$EquipmentSlot$HEAD;
            case BODY -> CoreReflections.instance$EquipmentSlot$BODY;
            case SADDLE -> CoreReflections.instance$EquipmentSlot$SADDLE;
        };
    }

    public static EquipmentSlot fromNMSEquipmentSlot(Object equipmentSlot) {
        Enum<?> directionEnum = (Enum<?>) equipmentSlot;
        int index = directionEnum.ordinal();
        return EquipmentSlot.values()[index];
    }
}
