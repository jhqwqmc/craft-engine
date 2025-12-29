package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;

public final class OverwritableItemNameProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<OverwritableItemNameProcessor> FACTORY = new Factory();
    private final ItemNameProcessor modifier;

    public OverwritableItemNameProcessor(String argument) {
        this.modifier = new ItemNameProcessor(argument);
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (VersionHelper.COMPONENT_RELEASE) {
            if (item.hasNonDefaultComponent(DataComponentKeys.ITEM_NAME)) {
                return item;
            }
        } else {
            if (item.hasTag("display", "Name")) {
                return item;
            }
        }
        return this.modifier.apply(item, context);
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.ITEM_NAME;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"display", "Name"};
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "display.Name";
    }

    private static class Factory implements ItemProcessorFactory<OverwritableItemNameProcessor> {

        @Override
        public OverwritableItemNameProcessor create(Object arg) {
            return new OverwritableItemNameProcessor(arg.toString());
        }
    }
}
