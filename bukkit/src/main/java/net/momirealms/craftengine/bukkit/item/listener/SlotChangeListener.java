package net.momirealms.craftengine.bukkit.item.listener;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class SlotChangeListener implements Listener {
    private final BukkitItemManager itemManager;

    public SlotChangeListener(BukkitItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSlotChange(final PlayerInventorySlotChangeEvent event) {
        ItemStack newItemStack = event.getNewItemStack();
        Item<ItemStack> wrap = this.itemManager.wrap(newItemStack);
        Optional<CustomItem<ItemStack>> optionalCustomItem = wrap.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            if (!customItem.settings().triggerAdvancement()) {
                event.setShouldTriggerAdvancements(false);
            }
        }
        this.itemManager.unlockRecipeOnInventoryChanged(event.getPlayer(), wrap);
    }
}
