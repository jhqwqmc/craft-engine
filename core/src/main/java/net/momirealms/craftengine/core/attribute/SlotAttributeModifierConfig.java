package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.Objects;
import java.util.function.Predicate;

public final class SlotAttributeModifierConfig extends AttributeModifierConfig {
    public final EquipmentSetSlot slot;

    public SlotAttributeModifierConfig(Key attribute, Key id, NumberProvider amount, Key operation, Predicate<Context> condition, EquipmentSetSlot slot) {
        super(attribute, id, amount, operation, condition);
        this.slot = slot;
    }

    public static SlotAttributeModifierConfig fromConfig(ConfigSection section) {
        Key attribute = section.getNonNullIdentifier("type");
        Key id = section.getNonNullIdentifier("id");
        NumberProvider amount = section.getNonNullNumber("amount");
        Key operation = section.getNonNullIdentifier("operation");
        EquipmentSetSlot setSlot = Objects.requireNonNull(EquipmentSetSlot.byName(section.getNonEmptyString("slot")));
        Predicate<Context> conditions = MiscUtils.allOf(section.getList("conditions", CommonConditions::fromConfig));
        return new SlotAttributeModifierConfig(attribute, id, amount, operation, conditions, setSlot);
    }
}
