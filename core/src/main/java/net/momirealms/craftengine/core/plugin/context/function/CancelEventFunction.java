package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Cancellable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CancelEventFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {

    public CancelEventFunction(List<Condition<CTX>> predicates) {
        super(predicates);
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<Cancellable> cancellable = ctx.getOptionalParameter(DirectContextParameters.EVENT);
        cancellable.ifPresent(value -> value.setCancelled(true));
    }

    public static <CTX extends Context> FunctionFactory<CTX, CancelEventFunction<CTX>> factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, CancelEventFunction<CTX>> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public CancelEventFunction<CTX> create(Map<String, Object> arguments) {
            return new CancelEventFunction<>(getPredicates(arguments));
        }
    }
}