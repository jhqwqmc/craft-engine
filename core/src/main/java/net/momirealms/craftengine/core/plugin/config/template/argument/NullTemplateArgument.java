package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class NullTemplateArgument implements TemplateArgument {
    public static final NullTemplateArgument INSTANCE = new NullTemplateArgument();
    public static final Factory FACTORY = new Factory();

    private NullTemplateArgument() {
    }

    @Override
    public Key type() {
        return TemplateArguments.NULL;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return null;
    }

    public static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            return NullTemplateArgument.INSTANCE;
        }
    }
}
