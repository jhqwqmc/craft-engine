package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class WhenTemplateArgument implements TemplateArgument {
    public static final Factory FACTORY = new Factory();
    private final TemplateArgument result;

    private WhenTemplateArgument(TemplateArgument result) {
        this.result = result;
    }

    @Override
    public Key type() {
        return TemplateArguments.WHEN;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.result.get(arguments);
    }

    public static class Factory implements TemplateArgumentFactory {

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
