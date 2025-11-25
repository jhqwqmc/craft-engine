package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class ConditionTemplateArgument implements TemplateArgument {
    public static final Factory FACTORY = new Factory();
    private final TemplateArgument result;

    private ConditionTemplateArgument(TemplateArgument result) {
        this.result = result;
    }

    @Override
    public Key type() {
        return TemplateArguments.CONDITION;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.result.get(arguments);
    }

    public static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            TemplateArgument onTrue = TemplateArguments.fromObject(ResourceConfigUtils.get(arguments, "on-true", "on_true"));
            TemplateArgument onFalse = TemplateArguments.fromObject(ResourceConfigUtils.get(arguments, "on-false", "on_false"));
            return new ConditionTemplateArgument(ResourceConfigUtils.getAsBoolean(arguments.get("condition"), "condition") ? onTrue : onFalse);
        }
    }
}
