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

import java.util.Map;

public final class PathMatchers {
    public static final PathMatcherType<AnyOfCondition<PathContext>> ANY_OF = register(Key.ce("any_of"), AnyOfCondition.factory(PathMatchers::fromMap));
    public static final PathMatcherType<AllOfCondition<PathContext>> ALL_OF = register(Key.ce("all_of"), AllOfCondition.factory(PathMatchers::fromMap));
    public static final PathMatcherType<InvertedCondition<PathContext>> INVERTED = register(Key.ce("inverted"), InvertedCondition.factory(PathMatchers::fromMap));
    public static final PathMatcherType<ContainsPathMatcher> CONTAINS = register(Key.ce("contains"), ContainsPathMatcher.FACTORY);
    public static final PathMatcherType<ExactPathMatcher> EXACT = register(Key.ce("exact"), ExactPathMatcher.FACTORY);
    public static final PathMatcherType<FilenamePathMatcher> FILENAME = register(Key.ce("filename"), FilenamePathMatcher.FACTORY);
    public static final PathMatcherType<PatternPathMatcher> PATTERN = register(Key.ce("pattern"), PatternPathMatcher.FACTORY);
    public static final PathMatcherType<ParentSuffixPathMatcher> PARENT_PATH_SUFFIX = register(Key.ce("parent_path_suffix"), ParentSuffixPathMatcher.FACTORY);
    public static final PathMatcherType<ParentPrefixPathMatcher> PARENT_PATH_PREFIX = register(Key.ce("parent_path_prefix"), ParentPrefixPathMatcher.FACTORY);

    private PathMatchers() {}

    public static <T extends Condition<PathContext>> PathMatcherType<T> register(Key key, ConditionFactory<PathContext, T> factory) {
        PathMatcherType<T> type = new PathMatcherType<>(key, factory);
        ((WritableRegistry<PathMatcherType<?>>) BuiltInRegistries.PATH_MATCHER_TYPE)
                .register(ResourceKey.create(Registries.PATH_MATCHER_TYPE.location(), key), type);
        return type;
    }

    public static Condition<PathContext> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), () -> new LocalizedException("warning.config.conflict_matcher.missing_type"));
        boolean reverted = type.charAt(0) == '!';
        if (reverted) {
            type = type.substring(1);
        }
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        PathMatcherType<? extends Condition<PathContext>> matcherType = BuiltInRegistries.PATH_MATCHER_TYPE.getValue(key);
        if (matcherType == null) {
            throw new LocalizedException("warning.config.conflict_matcher.invalid_type", type);
        }
        return reverted ? new InvertedCondition<>(matcherType.factory().create(map)) : matcherType.factory().create(map);
    }
}
