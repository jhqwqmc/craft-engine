package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ListTemplateArgument implements TemplateArgument {
    public static final Factory FACTORY = new Factory();
    private final List<Object> value;

    public ListTemplateArgument(List<Object> value) {
        this.value = value;
    }

    @Override
    public List<Object> get(Map<String, TemplateArgument> arguments) {
        return value;
    }

    @Override
    public Key type() {
        return TemplateArguments.LIST;
    }

    public static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            Object list = arguments.getOrDefault("list", List.of());
            return new ListTemplateArgument(castToListOrThrow(list, () -> new LocalizedResourceConfigException("warning.config.template.argument.list.invalid_type", list.getClass().getSimpleName())));
        }

        @SuppressWarnings("unchecked")
        private static List<Object> castToListOrThrow(Object obj, Supplier<LocalizedResourceConfigException> throwableSupplier) {
            if (obj instanceof List<?> list) {
                return (List<Object>) list;
            }
            throw throwableSupplier.get();
        }
    }
}
