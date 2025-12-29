package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

import java.util.List;
import java.util.Map;

public class RemoveEntityFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {

    public RemoveEntityFunction(List<Condition<CTX>> predicates) {
        super(predicates);
    }

    @Override
    public void runInternal(CTX ctx) {
        ctx.getOptionalParameter(DirectContextParameters.ENTITY).ifPresent(Entity::remove);
    }

    public static <CTX extends Context> FunctionFactory<CTX, RemoveEntityFunction<CTX>> factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, RemoveEntityFunction<CTX>> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public RemoveEntityFunction<CTX> create(Map<String, Object> arguments) {
            return new RemoveEntityFunction<>(getPredicates(arguments));
        }
    }
}