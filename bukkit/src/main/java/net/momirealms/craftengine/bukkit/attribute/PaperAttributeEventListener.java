package net.momirealms.craftengine.bukkit.attribute;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.util.EquipmentSlotUtils;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.attribute.AttributeContainer;
import net.momirealms.craftengine.core.attribute.EntityEquipments;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

public final class PaperAttributeEventListener implements Listener {
    private final BukkitAttributeManager manager;

    public PaperAttributeEventListener(BukkitAttributeManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (!Config.enableAttributeSystem()) return;
        LivingEntity entity = event.getEntity();
        AttributeContainer container = (AttributeContainer) this.manager.getOrCreateContainer(BukkitAdaptor.adapt(entity));
        if (container == null) {
            return;
        }
        EntityEquipments equipments = container.equipments();
        Map<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> equipmentChanges = event.getEquipmentChanges();
        for (Map.Entry<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> entry : equipmentChanges.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            equipments.remove(EquipmentSlotUtils.toEquipmentSetSlot(slot));
        }
        for (Map.Entry<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> entry : equipmentChanges.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            EntityEquipmentChangedEvent.EquipmentChange changed = entry.getValue();
            equipments.add(EquipmentSlotUtils.toEquipmentSetSlot(slot), ItemStackUtils.wrap(changed.newItem()));
        }
    }
}
