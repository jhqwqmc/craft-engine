package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.processor.ItemProcessor;

public interface ItemProcessorFactory<I> {

    ItemProcessor<I> create(Object arg);
}
