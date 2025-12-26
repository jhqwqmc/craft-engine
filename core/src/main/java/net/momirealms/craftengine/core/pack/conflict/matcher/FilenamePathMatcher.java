package net.momirealms.craftengine.core.pack.conflict.matcher;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public record FilenamePathMatcher(String name) implements Condition<PathContext> {
    public static final ConditionFactory<PathContext> FACTORY = new Factory();
    public static final Key ID = Key.of("craftengine:filename");

    @Override
    public boolean test(PathContext path) {
        String fileName = String.valueOf(path.path().getFileName());
        return fileName.equals(name);
    }

    private static class Factory implements ConditionFactory<PathContext> {
        @Override
        public Condition<PathContext> create(Map<String, Object> arguments) {
            String name = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("name"), () -> new LocalizedException("warning.config.conflict_matcher.filename.missing_name"));
            return new FilenamePathMatcher(name);
        }
    }
}
