package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class NullTemplateArgument implements TemplateArgument {
    public static final Key ID = Key.of("craftengine:null");
    public static final NullTemplateArgument INSTANCE = new NullTemplateArgument();
    public static final TemplateArgumentFactory FACTORY = new Factory();

    private NullTemplateArgument() {
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return null;
    }

    private static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            return NullTemplateArgument.INSTANCE;
        }
    }
}
