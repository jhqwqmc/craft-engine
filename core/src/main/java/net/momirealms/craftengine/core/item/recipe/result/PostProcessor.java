package net.momirealms.craftengine.core.item.recipe.result;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;

public interface PostProcessor<T> {

    Item<T> process(Item<T> item, ItemBuildContext context);
}
