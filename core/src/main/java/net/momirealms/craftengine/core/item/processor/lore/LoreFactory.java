package net.momirealms.craftengine.core.item.processor.lore;

import net.momirealms.craftengine.core.item.ItemProcessorFactory;

class LoreFactory implements ItemProcessorFactory<LoreProcessor> {
    @Override
    public LoreProcessor create(Object arg) {
        return LoreProcessor.createLoreModifier(arg);
    }
}