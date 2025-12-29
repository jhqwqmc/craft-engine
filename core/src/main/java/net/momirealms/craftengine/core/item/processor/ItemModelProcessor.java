package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;

public final class ItemModelProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<ItemModelProcessor> FACTORY = new Factory();
    private final Key data;

    public ItemModelProcessor(Key data) {
        this.data = data;
    }

    public Key data() {
        return this.data;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        return item.itemModel(this.data.asString());
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.ITEM_MODEL;
    }

    private static class Factory implements ItemProcessorFactory<ItemModelProcessor> {

        @Override
        public ItemModelProcessor create(Object arg) {
            String id = arg.toString();
            return new ItemModelProcessor(Key.of(id));
        }
    }
}
