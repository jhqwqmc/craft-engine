package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.Map;

public final class AlwaysTrueCondition<CTX extends Context> implements Condition<CTX> {
    public static final AlwaysTrueCondition<Context> INSTANCE = new AlwaysTrueCondition<Context>();

    @Override
    public boolean test(CTX ctx) {
        return true;
    }

    public static <CTX extends Context> ConditionFactory<CTX> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX> {

        @SuppressWarnings("unchecked")
        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            return (Condition<CTX>) INSTANCE;
        }
    }
}
