package net.momirealms.craftengine.bukkit.attribute;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

public final class PaperAttributeEventListener implements Listener {

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        Map<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> changed = event.getEquipmentChanges();

    }
}
