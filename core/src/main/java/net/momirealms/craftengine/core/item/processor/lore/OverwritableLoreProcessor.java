package net.momirealms.craftengine.core.item.processor.lore;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.processor.SimpleNetworkItemProcessor;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;

public final class OverwritableLoreProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<OverwritableLoreProcessor> FACTORY = new Factory();
    private final LoreProcessor loreProcessor;

    public OverwritableLoreProcessor(LoreProcessor loreProcessor) {
        this.loreProcessor = loreProcessor;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (VersionHelper.COMPONENT_RELEASE) {
            if (item.hasNonDefaultComponent(DataComponentKeys.LORE)) {
                return item;
            }
        } else {
            if (item.hasTag("display", "Lore")) {
                return item;
            }
        }
        return this.loreProcessor.apply(item, context);
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.LORE;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"display", "Lore"};
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "display.Lore";
    }

    @Override
    public <I> Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        if (VersionHelper.COMPONENT_RELEASE) {
            if (item.hasNonDefaultComponent(DataComponentKeys.LORE)) {
                return item;
            }
        } else {
            if (item.hasTag("display", "Lore")) {
                return item;
            }
        }
        return SimpleNetworkItemProcessor.super.prepareNetworkItem(item, context, networkData);
    }

    private static class Factory implements ItemProcessorFactory<OverwritableLoreProcessor> {
        @Override
        public OverwritableLoreProcessor create(Object arg) {
            LoreProcessor lore = LoreProcessor.createLoreModifier(arg);
            return new OverwritableLoreProcessor(lore);
        }
    }
}
