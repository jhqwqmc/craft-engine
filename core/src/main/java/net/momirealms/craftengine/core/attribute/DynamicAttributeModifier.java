package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Predicate;

public final class DynamicAttributeModifier implements AttributeModifier {
    private final AttributeModifierConfig config;

    public DynamicAttributeModifier(AttributeModifierConfig config) {
        this.config = config;
    }

    @Override
    public Predicate<Context> condition() {
        return this.config.condition;
    }

    @Override
    public Key id() {
        return this.config.id;
    }

    @Override
    public double amount(Context context) {
        return this.config.amount.getDouble(context);
    }

    @Override
    public Key operation() {
        return this.config.operation;
    }
}
