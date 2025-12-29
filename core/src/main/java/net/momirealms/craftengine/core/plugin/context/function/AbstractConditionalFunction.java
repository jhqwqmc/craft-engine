package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractConditionalFunction<CTX extends Context> implements Function<CTX> {
    protected final List<Condition<CTX>> predicates;
    private final Predicate<CTX> compositePredicates;

    public AbstractConditionalFunction(List<Condition<CTX>> predicates) {
        this.predicates = predicates;
        this.compositePredicates = MiscUtils.allOf(predicates);
    }

    @Override
    public void run(CTX ctx) {
        if (this.compositePredicates.test(ctx)) {
            this.runInternal(ctx);
        }
    }

    protected abstract void runInternal(CTX ctx);

    public static abstract class AbstractFactory<CTX extends Context, T extends Function<CTX>> implements FunctionFactory<CTX, T> {
        protected final java.util.function.Function<Map<String, Object>, Condition<CTX>> conditionFactory;

        public AbstractFactory(java.util.function.Function<Map<String, Object>, Condition<CTX>> conditionFactory) {
            this.conditionFactory = conditionFactory;
        }

        public java.util.function.Function<Map<String, Object>, Condition<CTX>> conditionFactory() {
            return this.conditionFactory;
        }

        protected List<Condition<CTX>> getPredicates(Map<String, Object> arguments) {
            if (arguments == null) return List.of();
            Object predicates = arguments.get("conditions");
            if (predicates == null) return List.of();
            switch (predicates) {
                case List<?> list -> {
                    List<Condition<CTX>> conditions = new ArrayList<>(list.size());
                    for (Object o : list) {
                        conditions.add(this.conditionFactory.apply(MiscUtils.castToMap(o, false)));
                    }
                    return conditions;
                }
                case Map<?, ?> map -> {
                    return List.of(this.conditionFactory.apply(MiscUtils.castToMap(map, false)));
                }
                default -> {
                    return List.of();
                }
            }
        }
    }

    public static abstract class AbstractFunctionalFactory<CTX extends Context, T extends Function<CTX>> extends AbstractFactory<CTX, T> {
        protected final java.util.function.Function<Map<String, Object>, Function<CTX>> functionFactory;

        public AbstractFunctionalFactory(java.util.function.Function<Map<String, Object>, Function<CTX>> functionFactory, java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
            this.functionFactory = functionFactory;
        }

        public java.util.function.Function<Map<String, Object>, Function<CTX>> functionFactory() {
            return functionFactory;
        }

        protected List<Function<CTX>> getFunctions(Map<String, Object> arguments) {
            if (arguments == null) return List.of();
            Object functions = arguments.get("functions");
            return parseFunctions(functions);
        }

        protected List<Function<CTX>> parseFunctions(Object functions) {
            if (functions == null) return List.of();
            switch (functions) {
                case List<?> list -> {
                    List<Function<CTX>> conditions = new ArrayList<>(list.size());
                    for (Object o : list) {
                        conditions.add(this.functionFactory.apply(MiscUtils.castToMap(o, false)));
                    }
                    return conditions;
                }
                case Map<?, ?> map -> {
                    return List.of(this.functionFactory.apply(MiscUtils.castToMap(map, false)));
                }
                default -> {
                    return List.of();
                }
            }
        }
    }
}
