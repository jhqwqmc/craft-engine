package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;

import java.util.Map;

public final class HasPlayerCondition<CTX extends Context> implements Condition<CTX> {

    public HasPlayerCondition() {
    }

    @Override
    public boolean test(CTX ctx) {
        if (ctx instanceof PlayerOptionalContext context) {
            return context.isPlayerPresent();
        }
        return false;
    }

    public static <CTX extends Context> ConditionFactory<CTX> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            return new HasPlayerCondition<>();
        }
    }
}
