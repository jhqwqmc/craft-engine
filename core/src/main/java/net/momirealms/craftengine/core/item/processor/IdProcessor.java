package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.Key;

public class IdProcessor<I> implements ItemProcessor<I> {
    public static final String CRAFT_ENGINE_ID = "craftengine:id";
    private final Key argument;

    public IdProcessor(Key argument) {
        this.argument = argument;
    }

    public Key identifier() {
        return this.argument;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.customId(this.argument);
        return item;
    }
}
