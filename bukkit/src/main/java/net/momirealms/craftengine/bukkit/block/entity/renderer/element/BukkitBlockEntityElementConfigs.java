package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigs;

public class BukkitBlockEntityElementConfigs extends BlockEntityElementConfigs {

    static {
        register(ITEM_DISPLAY, ItemDisplayBlockEntityElementConfig.FACTORY);
        register(TEXT_DISPLAY, TextDisplayBlockEntityElementConfig.FACTORY);
        register(ITEM, ItemBlockEntityElementConfig.FACTORY);
    }

    private BukkitBlockEntityElementConfigs() {}

    public static void init() {
    }
}
