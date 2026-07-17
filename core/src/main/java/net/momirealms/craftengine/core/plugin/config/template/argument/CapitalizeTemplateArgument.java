package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.StringUtils;

import java.util.Locale;
import java.util.Map;

public final class CapitalizeTemplateArgument implements TemplateArgument {
    public static final TemplateArgumentFactory<CapitalizeTemplateArgument> FACTORY = new Factory();
    private final String result;

    private CapitalizeTemplateArgument(String result) {
        this.result = result;
    }

    public static CapitalizeTemplateArgument capitalize(String result) {
        return new CapitalizeTemplateArgument(StringUtils.capitalize(result, Locale.getDefault()));
    }

    public String result() {
        return this.result;
    }

    @Override
    public Object get(String node, Map<String, TemplateArgument> arguments) {
        return this.result;
    }

    private static class Factory implements TemplateArgumentFactory<CapitalizeTemplateArgument> {

        @Override
        public CapitalizeTemplateArgument create(ConfigSection section) {
            return new CapitalizeTemplateArgument(StringUtils.capitalize(section.getNonNullString("value"), Locale.getDefault()));
        }
    }
}
