package net.momirealms.craftengine.core.attribute;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemDefinition;
import net.momirealms.craftengine.core.item.setting.value.EquipmentSetPart;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EntityEquipments {
    private final Map<EquipmentSetSlot, EquipmentSlotItem> equipments = new Object2ObjectOpenHashMap<>();
    private final AttributeContainer container;
    private Map<String, Integer> cachedActiveSets = Map.of();
    private boolean dirty;

    public EntityEquipments(AttributeContainer container) {
        this.container = container;
    }

    @Nullable
    public EquipmentSlotItem get(EquipmentSetSlot slot) {
        return this.equipments.get(slot);
    }

    @Nullable
    public EquipmentSlotItem add(EquipmentSetSlot slot, Item item) {
        EquipmentSlotItem newItem = EquipmentSlotItem.create(slot, item);
        EquipmentSlotItem previous = this.equipments.put(slot, newItem);
        if (previous != null) {
            previous.removeModifiers(this.container);
        }
        newItem.addOrUpdateModifiers(this.container);
        this.dirty = true;
        return previous;
    }

    @Nullable
    public EquipmentSlotItem remove(EquipmentSetSlot slot) {
        EquipmentSlotItem removed = this.equipments.remove(slot);
        if (removed != null) {
            removed.removeModifiers(this.container);
        }
        this.dirty = true;
        return removed;
    }

    public Map<String, Integer> getRawActiveSets() {
        if (this.dirty) {
            Map<String, Integer> setPartCount = this.cachedActiveSets;
            for (Map.Entry<EquipmentSetSlot, EquipmentSlotItem> entry : this.equipments.entrySet()) {
                Item item = entry.getValue().item();
                Optional<ItemDefinition> definition = item.getDefinition();
                definition.ifPresent(def -> {
                    EquipmentSetPart equipmentSetPart = def.settings().equipmentSetPart();
                    if (equipmentSetPart != null) {
                        List<String> matchingSets = equipmentSetPart.getMatchingSets(entry.getKey());
                        if (!matchingSets.isEmpty()) {
                            for (String set : matchingSets) {
                                setPartCount.compute(set, (k, v) -> v == null ? 1 : v + 1);
                            }
                        }
                    }
                });
            }
        }
        return this.cachedActiveSets;
    }
}
