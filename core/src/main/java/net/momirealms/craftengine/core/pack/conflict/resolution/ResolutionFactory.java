package net.momirealms.craftengine.core.pack.conflict.resolution;

import java.util.Map;

public interface ResolutionFactory<T extends Resolution> {

    T create(Map<String, Object> arguments);
}
