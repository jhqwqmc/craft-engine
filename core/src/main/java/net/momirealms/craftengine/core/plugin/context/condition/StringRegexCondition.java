package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class StringRegexCondition<CTX extends Context> implements Condition<CTX> {
    private final TextProvider value;
    private final TextProvider regex;

    public StringRegexCondition(TextProvider value, TextProvider regex) {
        this.value = value;
        this.regex = regex;
    }

    @Override
    public boolean test(CTX ctx) {
        return this.value.get(ctx).matches(this.regex.get(ctx));
    }

    public static <CTX extends Context> ConditionFactory<CTX, StringRegexCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, StringRegexCondition<CTX>> {

        @Override
        public StringRegexCondition<CTX> create(Map<String, Object> arguments) {
            String value = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("value"), "warning.config.condition.string_regex.missing_value");
            String regex = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("regex"), "warning.config.condition.string_regex.missing_regex");
            return new StringRegexCondition<>(TextProviders.fromString(value), TextProviders.fromString(regex));
        }
    }
}