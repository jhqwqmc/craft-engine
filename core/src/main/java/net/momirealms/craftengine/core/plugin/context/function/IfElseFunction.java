package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.Pair;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class IfElseFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final List<Pair<Predicate<CTX>, Function<CTX>>> conditions;

    public IfElseFunction(List<Condition<CTX>> predicates, List<Pair<Predicate<CTX>, Function<CTX>>> conditions) {
        super(predicates);
        this.conditions = conditions;
    }

    @Override
    public void runInternal(CTX ctx) {
        for (Pair<Predicate<CTX>, Function<CTX>> condition : this.conditions) {
            if (condition.left().test(ctx)) {
                condition.right().run(ctx);
                break;
            }
        }
    }

    public static class Factory<CTX extends Context> extends AbstractFunctionalFactory<CTX> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> conditionFactory, java.util.function.Function<Map<String, Object>, Function<CTX>> functionFactory) {
            super(conditionFactory, functionFactory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            List<Pair<Predicate<CTX>, Function<CTX>>> branches = ResourceConfigUtils.parseConfigAsList(
                    ResourceConfigUtils.requireNonNullOrThrow(ResourceConfigUtils.get(arguments, "rules", "rule"), "warning.config.function.if_else.missing_rules"),
                    map -> {
                        List<Condition<CTX>> conditions = getPredicates(map);
                        List<Function<CTX>> functions = getFunctions(map);
                        return new Pair<>(MiscUtils.allOf(conditions), Function.allOf(functions));
                    }
            );
            return new IfElseFunction<>(getPredicates(arguments), branches);
        }
    }
}
