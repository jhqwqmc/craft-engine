package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.sparrow.nbt.CompoundTag;

public interface ItemProcessor {

    <I> Item<I> apply(Item<I> item, ItemBuildContext context);

    default <I> Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        return item;
    }
}
