package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Predicate;

public class ConstantAttributeModifier implements AttributeModifier {
    private final Key id;
    private final double amount;
    private final Key operation;
    private final Predicate<Context> condition;

    public ConstantAttributeModifier(Key id, double amount, Key operation, Predicate<Context> condition) {
        this.id = id;
        this.amount = amount;
        this.operation = operation;
        this.condition = condition;
    }

    public static ConstantAttributeModifier simple(Key id, double amount, Key operation) {
        return new ConstantAttributeModifier(id, amount, operation, (c) -> true);
    }

    public static ConstantAttributeModifier conditional(Key id, double amount, Key operation, Predicate<Context> condition) {
        return new ConstantAttributeModifier(id, amount, operation, condition);
    }

    @Override
    public Predicate<Context> condition() {
        return this.condition;
    }

    @Override
    public Key id() {
        return this.id;
    }

    @Override
    public double amount(Context context) {
        return this.amount;
    }

    @Override
    public Key operation() {
        return this.operation;
    }
}
