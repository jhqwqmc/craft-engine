package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MItems;
import net.momirealms.craftengine.core.item.Item;
import org.bukkit.inventory.ItemStack;

public final class BukkitItemUtils {

    private BukkitItemUtils() {}

    public static boolean isDebugStick(Item<ItemStack> item) {
        return FastNMS.INSTANCE.method$ItemStack$getItem(item.getLiteralObject()) == MItems.DEBUG_STICK;
    }
}
