package net.momirealms.craftengine.core.plugin.config.template.argument;

import java.util.Map;

public interface TemplateArgumentFactory<T extends TemplateArgument> {

    T create(Map<String, Object> arguments);
}
