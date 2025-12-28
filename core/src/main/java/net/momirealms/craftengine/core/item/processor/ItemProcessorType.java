package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;

public record ItemProcessorType<T>(Key id, ItemProcessorFactory<T> factory) {
}
