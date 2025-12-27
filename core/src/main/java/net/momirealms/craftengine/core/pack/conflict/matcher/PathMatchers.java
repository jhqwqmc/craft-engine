package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.condition.AllOfCondition;
import net.momirealms.craftengine.core.plugin.context.condition.AnyOfCondition;
import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.plugin.context.condition.InvertedCondition;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PathMatchers {
    public static final PathMatcherType ANY_OF = register(Key.ce("any_of"), AnyOfCondition.factory(PathMatchers::fromMap));
    public static final PathMatcherType ALL_OF = register(Key.ce("all_of"), AllOfCondition.factory(PathMatchers::fromMap));
    public static final PathMatcherType INVERTED = register(Key.ce("inverted"), InvertedCondition.factory(PathMatchers::fromMap));
    public static final PathMatcherType CONTAINS = register(Key.ce("contains"), ContainsPathMatcher.FACTORY);
    public static final PathMatcherType EXACT = register(Key.ce("exact"), ExactPathMatcher.FACTORY);
    public static final PathMatcherType FILENAME = register(Key.ce("filename"), FilenamePathMatcher.FACTORY);
    public static final PathMatcherType PATTERN = register(Key.ce("pattern"), PatternPathMatcher.FACTORY);
    public static final PathMatcherType PARENT_PATH_SUFFIX = register(Key.ce("parent_path_suffix"), ParentSuffixPathMatcher.FACTORY);
    public static final PathMatcherType PARENT_PATH_PREFIX = register(Key.ce("parent_path_prefix"), ParentPrefixPathMatcher.FACTORY);

    private PathMatchers() {}

    public static PathMatcherType register(Key key, ConditionFactory<PathContext> factory) {
        PathMatcherType type = new PathMatcherType(key, factory);
        ((WritableRegistry<PathMatcherType>) BuiltInRegistries.PATH_MATCHER_TYPE)
                .register(ResourceKey.create(Registries.PATH_MATCHER_TYPE.location(), key), type);
        return type;
    }

    public static List<Condition<PathContext>> fromMapList(List<Map<String, Object>> arguments) {
        List<Condition<PathContext>> matchers = new ArrayList<>();
        for (Map<String, Object> term : arguments) {
            matchers.add(PathMatchers.fromMap(term));
        }
        return matchers;
    }

    public static Condition<PathContext> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), () -> new LocalizedException("warning.config.conflict_matcher.missing_type"));
        boolean reverted = type.charAt(0) == '!';
        if (reverted) {
            type = type.substring(1);
        }
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        PathMatcherType matcherType = BuiltInRegistries.PATH_MATCHER_TYPE.getValue(key);
        if (matcherType == null) {
            throw new LocalizedException("warning.config.conflict_matcher.invalid_type", type);
        }
        return reverted ? new InvertedCondition<>(matcherType.factory().create(map)) : matcherType.factory().create(map);
    }
}
