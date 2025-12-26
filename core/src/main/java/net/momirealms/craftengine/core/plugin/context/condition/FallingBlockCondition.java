package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

import java.util.Map;

public final class FallingBlockCondition<CTX extends Context> implements Condition<CTX> {

    @Override
    public boolean test(CTX ctx) {
        return ctx.getOptionalParameter(DirectContextParameters.FALLING_BLOCK).orElse(false);
    }

    public static <CTX extends Context> ConditionFactory<CTX> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            return new FallingBlockCondition<>();
        }
    }
}
