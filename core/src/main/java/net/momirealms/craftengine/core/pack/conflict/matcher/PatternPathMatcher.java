package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.regex.Pattern;

public final class PatternPathMatcher implements Condition<PathContext> {
    public static final ConditionFactory<PathContext, PatternPathMatcher> FACTORY = new Factory();
    private final Pattern pattern;

    public PatternPathMatcher(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public PatternPathMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern pattern() {
        return this.pattern;
    }

    @Override
    public boolean test(PathContext path) {
        String pathStr = CharacterUtils.replaceBackslashWithSlash(path.path().toString());
        return this.pattern.matcher(pathStr).matches();
    }

    private static class Factory implements ConditionFactory<PathContext, PatternPathMatcher> {
        @Override
        public PatternPathMatcher create(Map<String, Object> arguments) {
            String pattern = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("pattern"), () -> new LocalizedException("warning.config.conflict_matcher.pattern.missing_pattern"));
            return new PatternPathMatcher(pattern);
        }
    }
}