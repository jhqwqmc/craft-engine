package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ListTemplateArgument implements TemplateArgument {
    public static final TemplateArgumentFactory<ListTemplateArgument> FACTORY = new Factory();
    private final List<Object> value;

    private ListTemplateArgument(List<Object> value) {
        this.value = value;
    }

    public static ListTemplateArgument list(List<Object> value) {
        return new ListTemplateArgument(value);
    }

    @Override
    public List<Object> get(Map<String, TemplateArgument> arguments) {
        return this.value;
    }

    private static class Factory implements TemplateArgumentFactory<ListTemplateArgument> {

        @Override
        public ListTemplateArgument create(Map<String, Object> arguments) {
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
