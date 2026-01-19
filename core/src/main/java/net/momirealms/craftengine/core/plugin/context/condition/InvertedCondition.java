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

public final class InvertedCondition<CTX extends Context> implements Condition<CTX> {
    protected final Condition<CTX> condition;

    public InvertedCondition(Condition<CTX> condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(CTX ctx) {
        return !this.condition.test(ctx);
    }

    public static <CTX extends Context> ConditionFactory<CTX, InvertedCondition<CTX>> factory(Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private record Factory<CTX extends Context>(Function<Map<String, Object>, Condition<CTX>> factory) implements ConditionFactory<CTX, InvertedCondition<CTX>> {

        @SuppressWarnings("unchecked")
        @Override
        public InvertedCondition<CTX> create(Map<String, Object> arguments) {
            Object termObj = ResourceConfigUtils.requireNonNullOrThrow(
                    ResourceConfigUtils.get(arguments, "term", "terms"),
                    "warning.config.condition.inverted.missing_term"
            );
            if (termObj instanceof Map<?, ?> map) {
                return new InvertedCondition<>(this.factory.apply(MiscUtils.castToMap(map, false)));
            } else if (termObj instanceof List<?> list) {
                List<Condition<CTX>> conditions = new ArrayList<>();
                for (Map<String, Object> term : (List<Map<String, Object>>) list) {
                    conditions.add(factory.apply(term));
                }
                return new InvertedCondition<>(new AllOfCondition<>(conditions));
            } else {
                throw new LocalizedResourceConfigException("warning.config.condition.inverted.invalid_term_type", termObj.getClass().getSimpleName());
            }
        }
    }
}