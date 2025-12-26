package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public class MaxDamageProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final Key ID = Key.of("craftengine:max_damage");
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final NumberProvider argument;

    public MaxDamageProcessor(NumberProvider argument) {
        this.argument = argument;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.maxDamage(argument.getInt(context));
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.MAX_DAMAGE;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            NumberProvider numberProvider = NumberProviders.fromObject(arg);
            return new MaxDamageProcessor<>(numberProvider);
        }
    }
}
