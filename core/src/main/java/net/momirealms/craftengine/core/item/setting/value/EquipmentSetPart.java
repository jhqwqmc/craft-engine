package net.momirealms.craftengine.core.item.setting.value;

import net.momirealms.craftengine.core.attribute.EquipmentSetComponent;
import net.momirealms.craftengine.core.attribute.EquipmentSetSlot;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentSetPart {
    private final List<EquipmentSetComponent> components;

    public EquipmentSetPart(List<EquipmentSetComponent> components) {
        this.components = components;
    }

    public List<String> getMatchingSets(EquipmentSetSlot slot) {
        List<String> sets = new ArrayList<>(1);
        for (EquipmentSetComponent component : this.components) {
            if (component.slots().contains(slot)) {
                sets.addAll(component.sets());
            }
        }
        return sets;
    }
}
