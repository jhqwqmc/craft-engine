package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.util.Map;

public record ParentSuffixPathMatcher(String suffix) implements Condition<PathContext> {
    public static final ConditionFactory<PathContext, ParentSuffixPathMatcher> FACTORY = new Factory();

    @Override
    public boolean test(PathContext path) {
        Path parent = path.path().getParent();
        if (parent == null) return false;
        String pathStr = CharacterUtils.replaceBackslashWithSlash(parent.toString());
        return pathStr.endsWith(suffix);
    }

    private static class Factory implements ConditionFactory<PathContext, ParentSuffixPathMatcher> {
        @Override
        public ParentSuffixPathMatcher create(Map<String, Object> arguments) {
            String suffix = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("suffix"), () -> new LocalizedException("warning.config.conflict_matcher.parent_suffix.missing_suffix"));
            return new ParentSuffixPathMatcher(suffix);
        }
    }
}