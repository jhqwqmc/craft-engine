package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.List;
import java.util.Map;

public final class TemplateArguments {
    public static final TemplateArgumentType PLAIN = register(PlainStringTemplateArgument.ID, PlainStringTemplateArgument.FACTORY);
    public static final TemplateArgumentType SELF_INCREASE_INT = register(SelfIncreaseIntTemplateArgument.ID, SelfIncreaseIntTemplateArgument.FACTORY);
    public static final TemplateArgumentType MAP = register(MapTemplateArgument.ID, MapTemplateArgument.FACTORY);
    public static final TemplateArgumentType LIST = register(ListTemplateArgument.ID, ListTemplateArgument.FACTORY);
    public static final TemplateArgumentType NULL = register(NullTemplateArgument.ID, NullTemplateArgument.FACTORY);
    public static final TemplateArgumentType EXPRESSION = register(ExpressionTemplateArgument.ID, ExpressionTemplateArgument.FACTORY);
    public static final TemplateArgumentType CONDITION = register(ConditionTemplateArgument.ID, ConditionTemplateArgument.FACTORY);
    public static final TemplateArgumentType TO_UPPER_CASE = register(ToUpperCaseTemplateArgument.ID, ToUpperCaseTemplateArgument.FACTORY);
    public static final TemplateArgumentType TO_LOWER_CASE = register(ToLowerCaseTemplateArgument.ID, ToLowerCaseTemplateArgument.FACTORY);
    public static final TemplateArgumentType OBJECT = register(ObjectTemplateArgument.ID, ObjectTemplateArgument.FACTORY);
    public static final TemplateArgumentType WHEN = register(WhenTemplateArgument.ID, WhenTemplateArgument.FACTORY);

    private TemplateArguments() {}

    public static TemplateArgumentType register(Key key, TemplateArgumentFactory factory) {
        TemplateArgumentType type = new TemplateArgumentType(key, factory);
        ((WritableRegistry<TemplateArgumentType>) BuiltInRegistries.TEMPLATE_ARGUMENT_TYPE)
                .register(ResourceKey.create(Registries.TEMPLATE_ARGUMENT_TYPE.location(), key), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static TemplateArgument fromObject(Object object) {
        return switch (object) {
            case null -> NullTemplateArgument.INSTANCE;
            case List<?> list -> ListTemplateArgument.list((List<Object>) list);
            case Map<?, ?> map -> fromMap((Map<String, Object>) map);
            default -> ObjectTemplateArgument.object(object);
        };
    }

    public static TemplateArgument fromMap(Map<String, Object> map) {
        Object type = map.get("type");
        if (!(type instanceof String type0) || map.containsKey("__skip_template_argument__")) {
            return MapTemplateArgument.map(map);
        } else {
            Key key = Key.withDefaultNamespace(type0, Key.DEFAULT_NAMESPACE);
            TemplateArgumentType argumentType = BuiltInRegistries.TEMPLATE_ARGUMENT_TYPE.getValue(key);
            if (argumentType == null) {
                throw new IllegalArgumentException("Unknown argument type: " + type);
            }
            return argumentType.factory().create(map);
        }
    }
}
