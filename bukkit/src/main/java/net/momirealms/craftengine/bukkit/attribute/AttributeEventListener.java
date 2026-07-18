package net.momirealms.craftengine.bukkit.attribute;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;

public final class AttributeEventListener implements Listener {
    private final BukkitAttributeManager manager;

    public AttributeEventListener(BukkitAttributeManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRemove(EntityRemoveEvent event) {
        Entity entity = event.getEntity();
        this.manager.removeContainer(entity.getUniqueId());
    }
}
