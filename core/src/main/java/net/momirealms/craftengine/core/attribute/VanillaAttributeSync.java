package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

import java.beans.Expression;

public final class VanillaAttributeSync {
    private final Key target;
    private final Expression expression;

    public VanillaAttributeSync(Key target, Expression expression) {
        this.target = target;
        this.expression = expression;
    }

    public Key vanillaAttributeId() {
        return this.target;
    }

    public Expression expression() {
        return this.expression;
    }
}
