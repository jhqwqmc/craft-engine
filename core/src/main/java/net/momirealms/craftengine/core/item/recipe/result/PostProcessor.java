package net.momirealms.craftengine.core.item.recipe.result;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;

public interface PostProcessor {

    <I> Item<I> process(Item<I> item, ItemBuildContext context);
}
