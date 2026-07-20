package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRemoveEvent;

public final class AttributeEventListener implements Listener {
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
}
