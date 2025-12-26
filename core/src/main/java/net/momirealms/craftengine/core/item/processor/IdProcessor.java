package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;

public class IdProcessor<I> implements ItemProcessor<I> {
    public static final Key ID = Key.of("craftengine:id");
    public static final String CRAFT_ENGINE_ID = "craftengine:id";
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
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

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            String id = arg.toString();
            return new IdProcessor<>(Key.of(id));
        }
    }
}
