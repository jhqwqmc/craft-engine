package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;

public final class TooltipStyleProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<TooltipStyleProcessor> FACTORY = new Factory();
    private final Key argument;

    public TooltipStyleProcessor(Key argument) {
        this.argument = argument;
    }

    public Key tooltipStyle() {
        return this.argument;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.tooltipStyle(argument.toString());
        return item;
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.TOOLTIP_STYLE;
    }

    private static class Factory implements ItemProcessorFactory<TooltipStyleProcessor> {

        @Override
        public TooltipStyleProcessor create(Object arg) {
            String id = arg.toString();
            return new TooltipStyleProcessor(Key.of(id));
        }
    }
}
