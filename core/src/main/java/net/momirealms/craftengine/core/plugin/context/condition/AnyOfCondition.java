package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public final class AnyOfCondition<CTX extends Context> implements Condition<CTX> {
    private final Predicate<CTX> condition;

    public AnyOfCondition(List<? extends Condition<CTX>> conditions) {
        this.condition = MiscUtils.anyOf(conditions);
    }

    @Override
    public boolean test(CTX ctx) {
        return this.condition.test(ctx);
    }

    public static <CTX extends Context> ConditionFactory<CTX> factory(Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private record Factory<CTX extends Context>(Function<Map<String, Object>, Condition<CTX>> factory) implements ConditionFactory<CTX> {

        @SuppressWarnings("unchecked")
        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            Object termsArg = ResourceConfigUtils.requireNonNullOrThrow(
                    ResourceConfigUtils.get(arguments, "terms", "term"),
                    "warning.config.condition.any_of.missing_terms"
            );
            if (termsArg instanceof Map<?, ?> map) {
                return new AnyOfCondition<>(List.of(factory.apply(MiscUtils.castToMap(map, false))));
            } else if (termsArg instanceof List<?> list) {
                List<Condition<CTX>> conditions = new ArrayList<>();
                for (Map<String, Object> term : (List<Map<String, Object>>) list) {
                    conditions.add(factory.apply(term));
                }
                return new AnyOfCondition<>(conditions);
            } else {
                throw new LocalizedResourceConfigException("warning.config.condition.any_of.invalid_terms_type", termsArg.getClass().getSimpleName());
            }
        }
    }
}
