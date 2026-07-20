package net.momirealms.craftengine.core.item.setting.value;

import net.momirealms.craftengine.core.attribute.AttributeModifierConfig;
import net.momirealms.craftengine.core.attribute.EquipmentSetSlot;
import net.momirealms.craftengine.core.attribute.SlotAttributeModifierConfig;

import java.util.ArrayList;
import java.util.List;

public final class AttributeModifiers {
    private final List<SlotAttributeModifierConfig> modifiers;

    public AttributeModifiers(List<SlotAttributeModifierConfig> modifiers) {
        this.modifiers = modifiers;
    }

    public List<SlotAttributeModifierConfig> modifiers() {
        return this.modifiers;
    }

    public List<AttributeModifierConfig> modifiers(EquipmentSetSlot slot) {
        List<AttributeModifierConfig> attributeModifiers = new ArrayList<>(this.modifiers.size());
        for (SlotAttributeModifierConfig config : this.modifiers) {
            if (config.slot == slot) {
                attributeModifiers.add(config);
            }
        }
        return attributeModifiers;
    }
}
