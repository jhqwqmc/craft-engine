package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class PlainStringTemplateArgument implements TemplateArgument {
    public static final Factory FACTORY = new Factory();
    private final String value;

    public PlainStringTemplateArgument(String value) {
        this.value = value;
    }

    public static PlainStringTemplateArgument plain(final String value) {
        return new PlainStringTemplateArgument(value);
    }

    @Override
    public String get(Map<String, TemplateArgument> arguments) {
        return value;
    }

    @Override
    public Key type() {
        return TemplateArguments.PLAIN;
    }

    public static class Factory implements TemplateArgumentFactory {
        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            return new PlainStringTemplateArgument(arguments.getOrDefault("value", "").toString());
        }
    }
}
