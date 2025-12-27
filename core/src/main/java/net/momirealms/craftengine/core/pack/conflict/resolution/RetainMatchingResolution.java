package net.momirealms.craftengine.core.pack.conflict.resolution;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.pack.conflict.matcher.PathMatchers;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public record RetainMatchingResolution(Condition<PathContext> matcher) implements Resolution {
    public static final ResolutionFactory FACTORY = new Factory();

    @Override
    public void run(PathContext existing, PathContext conflict) {
        if (this.matcher.test(conflict)) {
            try {
                Files.copy(conflict.path(), existing.path(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                CraftEngine.instance().logger().warn("Failed to copy conflict file " + conflict + " to " + existing, e);
            }
        }
    }

    private static class Factory implements ResolutionFactory {

        @Override
        public Resolution create(Map<String, Object> arguments) {
            Map<String, Object> term = MiscUtils.castToMap(arguments.get("term"), false);
            return new RetainMatchingResolution(PathMatchers.fromMap(term));
        }
    }
}
