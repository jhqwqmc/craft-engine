package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.List;
import java.util.Map;

public final class TemplateArguments {
    public static final TemplateArgumentType<PlainStringTemplateArgument> PLAIN = register(Key.ce("plain"), PlainStringTemplateArgument.FACTORY);
    public static final TemplateArgumentType<SelfIncreaseIntTemplateArgument> SELF_INCREASE_INT = register(Key.ce("self_increase_int"), SelfIncreaseIntTemplateArgument.FACTORY);
    public static final TemplateArgumentType<MapTemplateArgument> MAP = register(Key.ce("map"), MapTemplateArgument.FACTORY);
    public static final TemplateArgumentType<ListTemplateArgument> LIST = register(Key.ce("list"), ListTemplateArgument.FACTORY);
    public static final TemplateArgumentType<NullTemplateArgument> NULL = register(Key.ce("null"), NullTemplateArgument.FACTORY);
    public static final TemplateArgumentType<ExpressionTemplateArgument> EXPRESSION = register(Key.ce("expression"), ExpressionTemplateArgument.FACTORY);
    public static final TemplateArgumentType<ConditionTemplateArgument> CONDITION = register(Key.ce("condition"), ConditionTemplateArgument.FACTORY);
    public static final TemplateArgumentType<ToUpperCaseTemplateArgument> TO_UPPER_CASE = register(Key.ce("to_upper_case"), ToUpperCaseTemplateArgument.FACTORY);
    public static final TemplateArgumentType<ToLowerCaseTemplateArgument> TO_LOWER_CASE = register(Key.ce("to_lower_case"), ToLowerCaseTemplateArgument.FACTORY);
    public static final TemplateArgumentType<ObjectTemplateArgument> OBJECT = register(Key.ce("object"), ObjectTemplateArgument.FACTORY);
    public static final TemplateArgumentType<WhenTemplateArgument> WHEN = register(Key.ce("when"), WhenTemplateArgument.FACTORY);

    private TemplateArguments() {}

    public static <T extends TemplateArgument> TemplateArgumentType<T> register(Key key, TemplateArgumentFactory<T> factory) {
        TemplateArgumentType<T> type = new TemplateArgumentType<>(key, factory);
        ((WritableRegistry<TemplateArgumentType<? extends TemplateArgument>>) BuiltInRegistries.TEMPLATE_ARGUMENT_TYPE)
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
            TemplateArgumentType<? extends TemplateArgument> argumentType = BuiltInRegistries.TEMPLATE_ARGUMENT_TYPE.getValue(key);
            if (argumentType == null) {
                throw new IllegalArgumentException("Unknown argument type: " + type);
            }
            return argumentType.factory().create(map);
        }
    }
}
