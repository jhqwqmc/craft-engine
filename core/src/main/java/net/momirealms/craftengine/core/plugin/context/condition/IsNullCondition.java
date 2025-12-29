package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.Optional;

public final class IsNullCondition<CTX extends Context> implements Condition<CTX> {
    private final ContextKey<?> key;

    public IsNullCondition(ContextKey<?> key) {
        this.key = key;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<?> optional = ctx.getOptionalParameter(this.key);
        return optional.isEmpty();
    }

    public static <CTX extends Context> ConditionFactory<CTX, IsNullCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, IsNullCondition<CTX>> {

        @Override
        public IsNullCondition<CTX> create(Map<String, Object> arguments) {
            String argument = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("argument"), "warning.config.condition.is_null.missing_argument");
            return new IsNullCondition<>(ContextKey.chain(argument));
        }
    }
}