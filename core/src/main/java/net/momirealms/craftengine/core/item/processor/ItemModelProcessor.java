package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.Nullable;

public class ItemModelProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final Key ID = Key.of("craftengine:item_model");
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final Key data;

    public ItemModelProcessor(Key data) {
        this.data = data;
    }

    public Key data() {
        return this.data;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
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
            return new ItemModelProcessor<>(Key.of(id));
        }
    }
}
