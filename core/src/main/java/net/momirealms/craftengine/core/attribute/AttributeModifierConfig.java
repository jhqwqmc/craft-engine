package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.function.Predicate;

public class AttributeModifierConfig {
    public final Key attribute;
    public final Key id;
    public final NumberProvider amount;
    public final Key operation;
    public final Predicate<Context> condition;

    public AttributeModifierConfig(Key attribute, Key id, NumberProvider amount, Key operation, Predicate<Context> condition) {
        this.attribute = attribute;
        this.id = id;
        this.amount = amount;
        this.operation = operation;
        this.condition = condition;
    }

    public DynamicAttributeModifier build() {
        return new DynamicAttributeModifier(this);
    }

    public static AttributeModifierConfig fromConfig(ConfigSection section) {
        Key attribute = section.getNonNullIdentifier("type");
        Key id = section.getNonNullIdentifier("id");
        NumberProvider amount = section.getNonNullNumber("amount");
        Key operation = section.getNonNullIdentifier("operation");
        Predicate<Context> conditions = MiscUtils.allOf(section.getList("conditions", CommonConditions::fromConfig));
        return new AttributeModifierConfig(attribute, id, amount, operation, conditions);
    }
}
