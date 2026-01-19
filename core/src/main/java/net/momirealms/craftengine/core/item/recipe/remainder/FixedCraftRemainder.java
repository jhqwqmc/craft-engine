package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.random.ThreadLocalRandomSource;

import java.util.Map;

public final class FixedCraftRemainder implements CraftRemainder {
    public static final CraftRemainderFactory<FixedCraftRemainder> FACTORY = new Factory();
    private final Key item;
    private final NumberProvider count;

    public FixedCraftRemainder(Key item, NumberProvider count) {
        this.item = item;
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Item<T> remainder(Key recipeId, Item<T> item) {
        Item<T> wrappedItem = (Item<T>) CraftEngine.instance().itemManager().createWrappedItem(this.item, null);
        if (wrappedItem != null) {
            wrappedItem.count(this.count.getInt(ThreadLocalRandomSource.INSTANCE));
        }
        return wrappedItem;
    }

    private static class Factory implements CraftRemainderFactory<FixedCraftRemainder> {

        @Override
        public FixedCraftRemainder create(Map<String, Object> args) {
            Key item = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(args.get("item"), "warning.config.item.settings.craft_remainder.fixed.missing_item"));
            NumberProvider count = NumberProviders.fromObject(args.getOrDefault("count", 1));
            return new FixedCraftRemainder(item, count);
        }
    }
}
