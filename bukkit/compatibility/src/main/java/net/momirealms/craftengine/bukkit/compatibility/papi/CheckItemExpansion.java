package net.momirealms.craftengine.bukkit.compatibility.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Predicate;

public class CheckItemExpansion extends PlaceholderExpansion {
    private final CraftEngine plugin;

    public CheckItemExpansion(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "checkceitem";
    }

    @Override
    public @NotNull String getAuthor() {
        return "jhqwqmc";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    /**
     * 用法:（小括号括起来的为必填，中括号括起来的为选填）
     * </br>
     * %checkceitem_count_(namespace):(path)%
     * </br>
     * %checkceitem_has_(namespace):(path):[amount]%
     * </br>
     * %checkceitem_id_[main_hand/off_hand/slot]%
     * </br>
     * %checkceitem_iscustom_[main_hand/off_hand/slot]%
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player bukkitPlayer, @NotNull String params) {
        if (bukkitPlayer == null) return null;
        BukkitServerPlayer player = BukkitCraftEngine.instance().adapt(bukkitPlayer);
        if (player == null) return null;
        int index = params.indexOf('_');
        String action = index > 0 ? params.substring(0, index) : params;
        String[] param = index > 0 ? params.substring(index + 1).split(":", 3) : new String[0];
        return switch (action) {
            case "count" -> param.length < 2 ? null : String.valueOf(getItemCount(player, param));
            case "has" -> {
                if (param.length < 2) yield null;
                int requiredAmount;
                try {
                    requiredAmount = param.length < 3 ? 1 : Integer.parseInt(param[2]);
                } catch (NumberFormatException e) {
                    yield null;
                }
                if (requiredAmount < 1) yield "true";
                yield String.valueOf(getItemCount(player, param) >= requiredAmount);
            }
            case "id" -> {
                Item<?> item = getItem(player, param);
                if (item == null) yield null;
                yield item.id().asString();
            }
            case "iscustom" -> {
                Item<?> item = getItem(player, param);
                if (item == null) yield null;
                yield String.valueOf(item.isCustomItem());
            }
            default -> null;
        };
    }

    @Nullable
    private Item<?> getItem(BukkitServerPlayer player, String[] param) {
        if (param.length < 1 || param[0] == null || param[0].isEmpty()) {
            return player.getItemInHand(InteractionHand.MAIN_HAND);
        }
        return switch (param[0].toLowerCase(Locale.ROOT)) {
            case "main_hand" -> player.getItemInHand(InteractionHand.MAIN_HAND);
            case "off_hand" -> player.getItemInHand(InteractionHand.OFF_HAND);
            default -> {
                try {
                    int slot = Integer.parseInt(param[0]);
                    yield player.getItemBySlot(Math.max(slot, 0));
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }

    private int getItemCount(BukkitServerPlayer player, String[] param) {
        Key itemId = Key.of(param[0], param[1]);
        Predicate<Object> predicate = nmsStack -> this.plugin.itemManager().wrap(ItemStackUtils.asCraftMirror(nmsStack)).id().equals(itemId);
        Object inventory = FastNMS.INSTANCE.method$Player$getInventory(player.serverPlayer());
        Object inventoryMenu = FastNMS.INSTANCE.field$Player$inventoryMenu(player.serverPlayer());
        Object craftSlots = FastNMS.INSTANCE.method$InventoryMenu$getCraftSlots(inventoryMenu);
        return FastNMS.INSTANCE.method$Inventory$clearOrCountMatchingItems(inventory, predicate, 0, craftSlots);
    }
}
