package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.Map;

public final class MapTemplateArgument implements TemplateArgument {
    public static final Key ID = Key.of("craftengine:map");
    public static final TemplateArgumentFactory FACTORY = new Factory();
    private final Map<String, Object> value;

    private MapTemplateArgument(Map<String, Object> value) {
        this.value = value;
    }

    public static MapTemplateArgument map(Map<String, Object> value) {
        return new MapTemplateArgument(value);
    }

    public Map<String, Object> value() {
        return this.value;
    }

    @Override
    public Map<String, Object> get(Map<String, TemplateArgument> arguments) {
        return this.value;
    }

    private static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            return new MapTemplateArgument(MiscUtils.castToMap(arguments.getOrDefault("map", Map.of()), false));
        }
    }
}
