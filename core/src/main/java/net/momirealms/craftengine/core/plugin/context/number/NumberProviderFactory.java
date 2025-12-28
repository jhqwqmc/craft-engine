package net.momirealms.craftengine.core.plugin.context.number;

import java.util.Map;

public interface NumberProviderFactory<T extends NumberProvider> {

    T create(Map<String, Object> args);
}
