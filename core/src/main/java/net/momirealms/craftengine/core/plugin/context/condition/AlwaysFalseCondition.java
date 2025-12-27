package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.Map;

public final class AlwaysFalseCondition<CTX extends Context> implements Condition<CTX> {
    public static final AlwaysFalseCondition<Context> INSTANCE = new AlwaysFalseCondition<>();

    public static <CTX extends Context> ConditionFactory<CTX> factory() {
        return new FactoryImpl<>();
    }

    private static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @SuppressWarnings("unchecked")
        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            return (Condition<CTX>) INSTANCE;
        }
    }
}
