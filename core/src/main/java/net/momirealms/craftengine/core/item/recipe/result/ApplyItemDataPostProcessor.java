package net.momirealms.craftengine.core.item.recipe.result;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;

public class ApplyItemDataPostProcessor<T> implements PostProcessor<T> {
    private final ItemProcessor<T>[] modifiers;

    public ApplyItemDataPostProcessor(ItemProcessor<T>[] modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public Item<T> process(Item<T> item, ItemBuildContext context) {
        for (ItemProcessor<T> modifier : this.modifiers) {
            item.apply(modifier, context);
        }
        return item;
    }
}