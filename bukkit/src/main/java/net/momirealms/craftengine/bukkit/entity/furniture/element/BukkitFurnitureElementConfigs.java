package net.momirealms.craftengine.bukkit.entity.furniture.element;

import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigs;

public class BukkitFurnitureElementConfigs extends FurnitureElementConfigs {

    static {
        register(ITEM_DISPLAY, ItemDisplayFurnitureElementConfig.FACTORY);
    }

    private BukkitFurnitureElementConfigs() {}

    public static void init() {
    }
}
