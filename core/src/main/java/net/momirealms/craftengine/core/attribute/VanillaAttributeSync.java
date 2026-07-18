package net.momirealms.craftengine.core.attribute;

import com.ezylang.evalex.Expression;
import net.momirealms.craftengine.core.util.Key;

public final class VanillaAttributeSync {
    private final Key target;
    private final Expression expression;

    public VanillaAttributeSync(Key target, Expression expression) {
        this.target = target;
        this.expression = expression;
    }

    public Key target() {
        return this.target;
    }

    public Expression expression() {
        return this.expression;
    }
}
