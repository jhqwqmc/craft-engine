package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.processor.ItemProcessor;

public interface ItemProcessorFactory<T extends ItemProcessor> {

    T create(Object arg);
}
