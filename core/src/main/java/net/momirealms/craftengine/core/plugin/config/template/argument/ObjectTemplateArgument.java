package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class ObjectTemplateArgument implements TemplateArgument {
    public static final Key ID = Key.of("craftengine:object");
    public static final TemplateArgumentFactory FACTORY = new Factory();
    private final Object value;

    private ObjectTemplateArgument(Object value) {
        this.value = value;
    }

    public static ObjectTemplateArgument object(Object value) {
        return new ObjectTemplateArgument(value);
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.value;
    }

    private static class Factory implements TemplateArgumentFactory {
        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            return new ObjectTemplateArgument(arguments.get("value"));
        }
    }
}
