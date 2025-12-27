package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;

public final class CompositeCraftRemainder implements CraftRemainder {
    private final CraftRemainder[] remainders;

    public CompositeCraftRemainder(CraftRemainder[] remainders) {
        this.remainders = remainders;
    }

    @Override
    public <T> Item<T> remainder(Key recipeId, Item<T> item) {
        for (CraftRemainder remainder : remainders) {
            item = remainder.remainder(recipeId, item);
        }
        return item;
    }
}
