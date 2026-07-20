package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemDefinition;
import net.momirealms.craftengine.core.item.setting.value.AttributeModifiers;

import java.util.List;
import java.util.Optional;

public final class EquipmentSlotItem {
    private final Item item;
    private final List<AttributeModifierConfig> snapshots;

    private EquipmentSlotItem(Item item, List<AttributeModifierConfig> snapshots) {
        this.item = item;
        this.snapshots = snapshots;
    }

    public static EquipmentSlotItem create(EquipmentSetSlot slot, Item item) {
        Optional<ItemDefinition> definition = item.getDefinition();
        if (definition.isEmpty()) return new EquipmentSlotItem(item, List.of());
        AttributeModifiers attributeModifiers = definition.get().settings().attributeModifiers();
        if (attributeModifiers == null) return new EquipmentSlotItem(item, List.of());
        return new EquipmentSlotItem(item, attributeModifiers.modifiers(slot));
    }

    public Item item() {
        return this.item;
    }

    public List<AttributeModifierConfig> snapshots() {
        return this.snapshots;
    }

    public void addOrUpdateModifiers(AttributeContainer container) {
        for (AttributeModifierConfig config : this.snapshots) {
            AttributeInstance instance = container.getOrCreateInstance(config.attribute);
            instance.addOrUpdateModifier(config.build());
        }
    }

    public void removeModifiers(AttributeContainer container) {
        for (AttributeModifierConfig config : this.snapshots) {
            AttributeInstance instance = container.getOrCreateInstance(config.attribute);
            instance.removeModifier(config.id);
        }
    }
}
