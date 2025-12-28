package net.momirealms.craftengine.core.item.processor.lore;

import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;

class LoreFactory<I> implements ItemProcessorFactory<I> {
    @Override
    public ItemProcessor create(Object arg) {
        return LoreProcessor.createLoreModifier(arg);
    }
}