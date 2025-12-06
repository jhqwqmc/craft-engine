package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public interface TemplateArgument {

    Key type();

    Object get(Map<String, TemplateArgument> arguments);
}
