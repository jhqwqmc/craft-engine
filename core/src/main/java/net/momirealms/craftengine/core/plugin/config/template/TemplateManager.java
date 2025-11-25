package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.config.template.argument.TemplateArgument;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.SNBTReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface TemplateManager extends Manageable {

    TemplateManager INSTANCE = new TemplateManagerImpl();

    ConfigParser parser();

    Object applyTemplates(Key id, Object input);
}
