package net.momirealms.craftengine.core.plugin.config.template.argument;

import java.util.Map;

public final class PlainStringTemplateArgument implements TemplateArgument {
    public static final TemplateArgumentFactory<PlainStringTemplateArgument> FACTORY = new Factory();
    private final String value;

    private PlainStringTemplateArgument(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static PlainStringTemplateArgument plain(final String value) {
        return new PlainStringTemplateArgument(value);
    }

    @Override
    public String get(Map<String, TemplateArgument> arguments) {
        return this.value;
    }

    private static class Factory implements TemplateArgumentFactory<PlainStringTemplateArgument> {
        @Override
        public PlainStringTemplateArgument create(Map<String, Object> arguments) {
            return new PlainStringTemplateArgument(arguments.getOrDefault("value", "").toString());
        }
    }
}
