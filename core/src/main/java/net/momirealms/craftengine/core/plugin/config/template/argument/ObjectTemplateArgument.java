package net.momirealms.craftengine.core.plugin.config.template.argument;

import java.util.Map;

public final class ObjectTemplateArgument implements TemplateArgument {
    public static final TemplateArgumentFactory<ObjectTemplateArgument> FACTORY = new Factory();
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

    private static class Factory implements TemplateArgumentFactory<ObjectTemplateArgument> {
        @Override
        public ObjectTemplateArgument create(Map<String, Object> arguments) {
            return new ObjectTemplateArgument(arguments.get("value"));
        }
    }
}
