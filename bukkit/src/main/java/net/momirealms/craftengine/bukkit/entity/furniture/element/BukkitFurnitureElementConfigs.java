package net.momirealms.craftengine.bukkit.entity.furniture.element;

import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigs;

public class BukkitFurnitureElementConfigs extends FurnitureElementConfigs {

    static {
        register(ITEM_DISPLAY, ItemDisplayFurnitureElementConfig.FACTORY);
        register(TEXT_DISPLAY, TextDisplayFurnitureElementConfig.FACTORY);
        register(ITEM, ItemFurnitureElementConfig.FACTORY);
        register(ARMOR_STAND, ArmorStandFurnitureElementConfig.FACTORY);
    }

    private BukkitFurnitureElementConfigs() {}

    public static void init() {
    }
}
