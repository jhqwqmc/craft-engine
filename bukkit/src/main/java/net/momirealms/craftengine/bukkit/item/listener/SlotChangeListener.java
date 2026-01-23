package net.momirealms.craftengine.bukkit.item.listener;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.recipe.BukkitRecipeManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.IngredientUnlockable;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
        Key itemId = wrap.id();
        Player player = event.getPlayer();
        BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(player);
        if (serverPlayer == null) return;
        serverPlayer.addObtainedItem(itemId);
        List<IngredientUnlockable> recipes = BukkitRecipeManager.instance().ingredientUnlockablesByChangedItem(itemId);
        if (recipes.isEmpty()) return;
        List<NamespacedKey> recipesToUnlock = new ArrayList<>(4);
        for (IngredientUnlockable recipe : recipes) {
            NamespacedKey recipeBukkitId = KeyUtils.toNamespacedKey(recipe.id());
            if (!player.hasDiscoveredRecipe(recipeBukkitId)) {
                if (recipe.canUnlock(serverPlayer, serverPlayer.obtainedItems())) {
                    recipesToUnlock.add(recipeBukkitId);
                }
            }
        }
        if (!recipesToUnlock.isEmpty()) {
            player.discoverRecipes(recipesToUnlock);
        }
    }
}
