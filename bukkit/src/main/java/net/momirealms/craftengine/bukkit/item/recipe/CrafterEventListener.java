package net.momirealms.craftengine.bukkit.item.recipe;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.item.recipe.CustomCraftingTableRecipe;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CrafterEventListener implements Listener {
    private final ItemManager<ItemStack> itemManager;
    private final BukkitRecipeManager recipeManager;
    private final BukkitCraftEngine plugin;

    public CrafterEventListener(BukkitCraftEngine plugin, BukkitRecipeManager recipeManager, ItemManager<ItemStack> itemManager) {
        this.itemManager = itemManager;
        this.recipeManager = recipeManager;
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCrafterCraft(CrafterCraftEvent event) {
        CraftingRecipe recipe = event.getRecipe();
        Key recipeId = Key.of(recipe.getKey().namespace(), recipe.getKey().value());
        Optional<Recipe<ItemStack>> optionalRecipe = this.recipeManager.recipeById(recipeId);
        // 也许是其他插件注册的配方，直接无视
        if (optionalRecipe.isEmpty()) {
            return;
        }
        CustomCraftingTableRecipe<ItemStack> ceRecipe = (CustomCraftingTableRecipe<ItemStack>) optionalRecipe.get();
        if (ceRecipe.hasCondition()) {
            if (!ceRecipe.canUse(PlayerOptionalContext.of(null))) {
                event.setCancelled(true);
                return;
            }
        }
        if (ceRecipe.hasVisualResult() || ceRecipe.alwaysRebuildOutput()) {
            // 重新构建产物，保证papi最新
            event.setResult(ceRecipe.assemble(null, ItemBuildContext.empty()));
        }
        // 不执行functions了，估计没什么用
    }
}
