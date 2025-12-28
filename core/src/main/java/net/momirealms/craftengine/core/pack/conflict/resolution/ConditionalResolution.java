package net.momirealms.craftengine.core.pack.conflict.resolution;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.pack.conflict.matcher.PathMatchers;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.Map;

public record ConditionalResolution(Condition<PathContext> matcher, Resolution resolution) implements Resolution {
    public static final ResolutionFactory<ConditionalResolution> FACTORY = new Factory();

    @Override
    public void run(PathContext existing, PathContext conflict) {
        if (this.matcher.test(existing)) {
            this.resolution.run(existing, conflict);
        }
    }

    private static class Factory implements ResolutionFactory<ConditionalResolution> {

        @Override
        public ConditionalResolution create(Map<String, Object> arguments) {
            Map<String, Object> term = MiscUtils.castToMap(arguments.get("term"), false);
            Map<String, Object> resolution = MiscUtils.castToMap(arguments.get("resolution"), false);
            return new ConditionalResolution(PathMatchers.fromMap(term), Resolutions.fromMap(resolution));
        }
    }
}
