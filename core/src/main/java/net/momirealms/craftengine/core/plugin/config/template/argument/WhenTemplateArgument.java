package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class WhenTemplateArgument implements TemplateArgument {
    public static final Key ID = Key.of("craftengine:when");
    public static final TemplateArgumentFactory FACTORY = new Factory();
    private final TemplateArgument result;

    private WhenTemplateArgument(TemplateArgument result) {
        this.result = result;
    }

    public static WhenTemplateArgument when(TemplateArgument result) {
        return new WhenTemplateArgument(result);
    }

    public TemplateArgument result() {
        return this.result;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.result.get(arguments);
    }

    private static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            String source = ResourceConfigUtils.getAsStringOrNull(arguments.get("source"));
            TemplateArgument fallback = TemplateArguments.fromObject(arguments.get("fallback"));
            if (source == null) {
                return new WhenTemplateArgument(fallback);
            }
            Map<String, Object> whenMap = ResourceConfigUtils.getAsMap(arguments.get("when"), "when");
            TemplateArgument value = whenMap.containsKey(source) ? TemplateArguments.fromObject(whenMap.get(source)) : fallback;
            return new WhenTemplateArgument(value);
        }
    }
}
