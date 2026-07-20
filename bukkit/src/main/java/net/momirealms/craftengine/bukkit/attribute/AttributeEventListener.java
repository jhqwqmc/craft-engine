package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.util.EquipmentSlotUtils;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.attribute.AttributeContainer;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class AttributeEventListener implements Listener {
    private static final EquipmentSlot[] PLAYER_SLOTS = new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final BukkitAttributeManager manager;

    public AttributeEventListener(BukkitAttributeManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRemove(EntityRemoveEvent event) {
        if (!Config.enableAttributeSystem()) return;
        Entity entity = event.getEntity();
        this.manager.removeContainer(entity.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!Config.enableAttributeSystem()) return;
        BukkitDamageEvent damageEvent = new BukkitDamageEvent(this.manager, event);
        this.manager.processDamageEvent(damageEvent);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AttributeContainer container = (AttributeContainer) this.manager.getOrCreateContainer(BukkitAdaptor.adapt(player));
        PlayerInventory inventory = player.getInventory();
        for (EquipmentSlot slot : PLAYER_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            BukkitItem wrappedItem = ItemStackUtils.wrap(item);
            container.equipments().add(EquipmentSlotUtils.toEquipmentSetSlot(slot), wrappedItem);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.manager.removeContainer(event.getPlayer().getUniqueId());
    }
}
