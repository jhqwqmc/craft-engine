package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class ObjectTemplateArgument implements TemplateArgument {
    private final Object value;

    public ObjectTemplateArgument(Object value) {
        this.value = value;
    }

    public static ObjectTemplateArgument of(Object value) {
        return new ObjectTemplateArgument(value);
    }

    @Override
    public Key type() {
        return TemplateArguments.OBJECT;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.value;
    }
}
