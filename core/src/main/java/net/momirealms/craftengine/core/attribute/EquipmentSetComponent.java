package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

import java.util.List;

public final class EquipmentSetComponent {
    private final List<EquipmentSetSlot> slots;
    private final List<String> sets;

    public EquipmentSetComponent(List<EquipmentSetSlot> slots, List<String> sets) {
        this.slots = slots;
        this.sets = sets;
    }

    public static EquipmentSetComponent fromConfig(ConfigSection section) {
        List<EquipmentSetSlot> slots = section.getList("slots", v -> {
            EquipmentSetSlot slot = EquipmentSetSlot.byName(v.getAsString());
            if (slot == null) {
                // todo KnownException
                throw new IllegalArgumentException("Invalid equipment set slot name: " + v.getAsString());
            }
            return slot;
        });
        List<String> sets = section.getStringList("sets");
        return new EquipmentSetComponent(slots, sets);
    }

    public List<EquipmentSetSlot> slots() {
        return this.slots;
    }

    public List<String> sets() {
        return this.sets;
    }
}
