package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class FixedCraftRemainder implements CraftRemainder {
    public static final CraftRemainderFactory<FixedCraftRemainder> FACTORY = new Factory();
    private final Key item;

    public FixedCraftRemainder(Key item) {
        this.item = item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Item<T> remainder(Key recipeId, Item<T> item) {
        return (Item<T>) CraftEngine.instance().itemManager().createWrappedItem(this.item, null);
    }

    private static class Factory implements CraftRemainderFactory<FixedCraftRemainder> {

        @Override
        public FixedCraftRemainder create(Map<String, Object> args) {
            Key item = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(args.get("item"), "warning.config.item.settings.craft_remainder.fixed.missing_item"));
            return new FixedCraftRemainder(item);
        }
    }
}
