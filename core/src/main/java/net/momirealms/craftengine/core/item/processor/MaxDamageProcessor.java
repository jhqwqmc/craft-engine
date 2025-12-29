package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;

public final class MaxDamageProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<MaxDamageProcessor> FACTORY = new Factory();
    private final NumberProvider argument;

    public MaxDamageProcessor(NumberProvider argument) {
        this.argument = argument;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.maxDamage(argument.getInt(context));
        return item;
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.MAX_DAMAGE;
    }

    private static class Factory implements ItemProcessorFactory<MaxDamageProcessor> {

        @Override
        public MaxDamageProcessor create(Object arg) {
            NumberProvider numberProvider = NumberProviders.fromObject(arg);
            return new MaxDamageProcessor(numberProvider);
        }
    }
}
