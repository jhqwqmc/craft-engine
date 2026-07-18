package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Predicate;

public final class AttributeModifier {
    private final Key id;
    private final double amount;
    private final Key operation;
    private final Predicate<Context> condition;

    public AttributeModifier(Key id, double amount, Key operation, Predicate<Context> condition) {
        this.id = id;
        this.amount = amount;
        this.operation = operation;
        this.condition = condition;
    }

    public static AttributeModifier simple(Key id, double amount, Key operation) {
        return new AttributeModifier(id, amount, operation, (c) -> true);
    }

    public static AttributeModifier conditional(Key id, double amount, Key operation, Predicate<Context> condition) {
        return new AttributeModifier(id, amount, operation, condition);
    }

    public Predicate<Context> condition() {
        return condition;
    }

    public Key id() {
        return this.id;
    }

    public double amount() {
        return this.amount;
    }

    public Key operation() {
        return this.operation;
    }
}
