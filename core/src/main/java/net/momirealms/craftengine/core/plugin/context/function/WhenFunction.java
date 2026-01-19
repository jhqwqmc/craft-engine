package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.Pair;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhenFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final TextProvider source;
    private final Map<String, Function<CTX>> whenMap;
    private final Function<CTX> fallback;

    public WhenFunction(List<Condition<CTX>> predicates, TextProvider source, Map<String, Function<CTX>> whenMap, Function<CTX> fallback) {
        super(predicates);
        this.whenMap = whenMap;
        this.source = source;
        this.fallback = fallback;
    }

    @Override
    public void runInternal(CTX ctx) {
        String text = this.source.get(ctx);
        Function<CTX> function = this.whenMap.getOrDefault(text, this.fallback);
        function.run(ctx);
    }

    public static <CTX extends Context> FunctionFactory<CTX, WhenFunction<CTX>> factory(java.util.function.Function<Map<String, Object>, Function<CTX>> f1, java.util.function.Function<Map<String, Object>, Condition<CTX>> f2) {
        return new Factory<>(f1, f2);
    }

    private static class Factory<CTX extends Context> extends AbstractFunctionalFactory<CTX, WhenFunction<CTX>> {

        public Factory(java.util.function.Function<Map<String, Object>, Function<CTX>> functionFactory, java.util.function.Function<Map<String, Object>, Condition<CTX>> conditionFactory) {
            super(functionFactory, conditionFactory);
        }

        @Override
        public WhenFunction<CTX> create(Map<String, Object> arguments) {
            TextProvider source = TextProviders.fromString(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("source"), "warning.config.function.when.missing_source"));
            List<Pair<List<String>, Function<CTX>>> list = ResourceConfigUtils.parseConfigAsList(arguments.get("cases"), map -> {
                List<String> when = MiscUtils.getAsStringList(map.get("when"));
                List<Function<CTX>> functions = getFunctions(map);
                return Pair.of(when, Function.allOf(functions));
            });
            Map<String, Function<CTX>> whenMap = new HashMap<>();
            for (Pair<List<String>, Function<CTX>> pair : list) {
                for (String when : pair.left()) {
                    whenMap.put(when, pair.right());
                }
            }
            return new WhenFunction<>(getPredicates(arguments), source, whenMap, Function.allOf(parseFunctions(arguments.get("fallback"))));
        }
    }
}