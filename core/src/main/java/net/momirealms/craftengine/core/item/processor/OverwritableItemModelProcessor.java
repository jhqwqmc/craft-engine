package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;

public final class OverwritableItemModelProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<OverwritableItemModelProcessor> FACTORY = new Factory();
    private final Key data;

    public OverwritableItemModelProcessor(Key data) {
        this.data = data;
    }

    public Key data() {
        return data;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (item.hasNonDefaultComponent(DataComponentKeys.ITEM_MODEL)) return item;
        return item.itemModel(this.data.asString());
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.ITEM_MODEL;
    }

    private static class Factory implements ItemProcessorFactory<OverwritableItemModelProcessor> {

        @Override
        public OverwritableItemModelProcessor create(Object arg) {
            String id = arg.toString();
            return new OverwritableItemModelProcessor(Key.of(id));
        }
    }
}
