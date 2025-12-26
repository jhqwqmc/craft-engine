package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public class OverwritableItemModelProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final Key data;

    public OverwritableItemModelProcessor(Key data) {
        this.data = data;
    }

    public Key data() {
        return data;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (item.hasNonDefaultComponent(DataComponentKeys.ITEM_MODEL)) return item;
        return item.itemModel(this.data.asString());
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.ITEM_MODEL;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            String id = arg.toString();
            return new OverwritableItemModelProcessor<>(Key.of(id));
        }
    }
}
