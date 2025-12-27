package net.momirealms.craftengine.core.block.properties;

import java.util.Map;

public interface PropertyFactory<T extends Comparable<T>> {

    Property<T> create(String name, Map<String, Object> arguments);
}
