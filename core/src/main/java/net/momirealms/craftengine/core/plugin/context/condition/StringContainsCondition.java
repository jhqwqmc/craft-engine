package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class StringContainsCondition<CTX extends Context> implements Condition<CTX> {
    private final TextProvider value1;
    private final TextProvider value2;

    public StringContainsCondition(TextProvider value1, TextProvider value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public boolean test(CTX ctx) {
        return this.value1.get(ctx).contains(this.value2.get(ctx));
    }

    public static <CTX extends Context> ConditionFactory<CTX> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            String value1 = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("value1"), "warning.config.condition.string_contains.missing_value1");
            String value2 = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("value2"), "warning.config.condition.string_contains.missing_value2");
            return new StringContainsCondition<>(TextProviders.fromString(value1), TextProviders.fromString(value2));
        }
    }
}
