package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public record ExactPathMatcher(String path) implements Condition<PathContext> {
    public static final ConditionFactory<PathContext> FACTORY = new Factory();

    @Override
    public boolean test(PathContext path) {
        String pathStr = CharacterUtils.replaceBackslashWithSlash(path.path().toString());
        return pathStr.equals(this.path);
    }

    private static class Factory implements ConditionFactory<PathContext> {
        @Override
        public Condition<PathContext> create(Map<String, Object> arguments) {
            String path = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("path"), () -> new LocalizedException("warning.config.conflict_matcher.exact.missing_path"));
            return new ExactPathMatcher(path);
        }
    }
}
