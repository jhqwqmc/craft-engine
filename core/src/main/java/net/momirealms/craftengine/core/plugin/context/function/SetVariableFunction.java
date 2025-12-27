package net.momirealms.craftengine.core.plugin.context.function;

import com.mojang.datafixers.util.Either;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public class SetVariableFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final Either<TextProvider, NumberProvider> either;
    private final String variableName;
    private final boolean asInt;

    public SetVariableFunction(List<Condition<CTX>> predicates, String variableName, Either<TextProvider, NumberProvider> either, boolean asInt) {
        super(predicates);
        this.either = either;
        this.variableName = variableName;
        this.asInt = asInt;
    }

    @Override
    public void runInternal(CTX ctx) {
        ContextHolder contexts = ctx.contexts();
        if (contexts.immutable()) return;
        this.either.ifLeft(text -> contexts.withParameter(ContextKey.direct("var_" + this.variableName), text.get(ctx)))
                .ifRight(number -> contexts.withParameter(ContextKey.direct("var_" + this.variableName), asInt ? number.getInt(ctx) : number.getDouble(ctx)));
    }

    public static <CTX extends Context> FunctionFactory<CTX> factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            String variableName = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("name"), "warning.config.function.set_variable.missing_name");
            if (arguments.containsKey("number")) {
                return new SetVariableFunction<>(
                        getPredicates(arguments),
                        variableName,
                        Either.right(NumberProviders.fromObject(arguments.get("number"))),
                        ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("as-int", false), "as-int")
                );
            } else if (arguments.containsKey("text")) {
                return new SetVariableFunction<>(
                        getPredicates(arguments),
                        variableName,
                        Either.left(TextProviders.fromString(arguments.get("text").toString())),
                        false
                );
            } else {
                throw new LocalizedResourceConfigException("warning.config.function.set_variable.missing_value");
            }
        }
    }
}
