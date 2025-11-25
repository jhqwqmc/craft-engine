package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.List;
import java.util.Map;

public class TemplateArguments {
    public static final Key PLAIN = Key.of("craftengine:plain");
    public static final Key SELF_INCREASE_INT = Key.of("craftengine:self_increase_int");
    public static final Key MAP = Key.of("craftengine:map");
    public static final Key LIST = Key.of("craftengine:list");
    public static final Key NULL = Key.of("craftengine:null");
    public static final Key CONDITION = Key.of("craftengine:condition");
    public static final Key EXPRESSION = Key.of("craftengine:expression");
    public static final Key OBJECT = Key.of("craftengine:object"); // No Factory, internal use
    public static final Key TO_UPPER_CASE = Key.of("craftengine:to_upper_case");
    public static final Key TO_LOWER_CASE = Key.of("craftengine:to_lower_case");

    public static void register(Key key, TemplateArgumentFactory factory) {
        ((WritableRegistry<TemplateArgumentFactory>) BuiltInRegistries.TEMPLATE_ARGUMENT_FACTORY)
                .register(ResourceKey.create(Registries.TEMPLATE_ARGUMENT_FACTORY.location(), key), factory);
    }

    static {
        register(PLAIN, PlainStringTemplateArgument.FACTORY);
        register(SELF_INCREASE_INT, SelfIncreaseIntTemplateArgument.FACTORY);
        register(MAP, MapTemplateArgument.FACTORY);
        register(LIST, ListTemplateArgument.FACTORY);
        register(NULL, NullTemplateArgument.FACTORY);
        register(EXPRESSION, ExpressionTemplateArgument.FACTORY);
        register(CONDITION, ConditionTemplateArgument.FACTORY);
        register(TO_UPPER_CASE, ToUpperCaseTemplateArgument.FACTORY);
        register(TO_LOWER_CASE, ToLowerCaseTemplateArgument.FACTORY);
    }

    @SuppressWarnings("unchecked")
    public static TemplateArgument fromObject(Object object) {
        return switch (object) {
            case null -> NullTemplateArgument.INSTANCE;
            case List<?> list -> new ListTemplateArgument((List<Object>) list);
            case Map<?, ?> map -> fromMap((Map<String, Object>) map);
            default -> new ObjectTemplateArgument(object);
        };
    }

    public static TemplateArgument fromMap(Map<String, Object> map) {
        Object type = map.get("type");
        if (!(type instanceof String type0) || map.containsKey("__skip_template_argument__")) {
            return new MapTemplateArgument(map);
        } else {
            Key key = Key.withDefaultNamespace(type0, Key.DEFAULT_NAMESPACE);
            TemplateArgumentFactory factory = BuiltInRegistries.TEMPLATE_ARGUMENT_FACTORY.getValue(key);
            if (factory == null) {
                throw new IllegalArgumentException("Unknown argument type: " + type);
            }
            return factory.create(map);
        }
    }
}
