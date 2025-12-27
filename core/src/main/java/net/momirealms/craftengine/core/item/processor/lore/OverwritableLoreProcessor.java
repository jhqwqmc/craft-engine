package net.momirealms.craftengine.core.item.processor.lore;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.SimpleNetworkItemProcessor;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;

public final class OverwritableLoreProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final LoreProcessor<I> loreProcessor;

    public OverwritableLoreProcessor(LoreProcessor<I> loreProcessor) {
        this.loreProcessor = loreProcessor;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
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
    public Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.LORE;
    }

    @Override
    public Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"display", "Lore"};
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "display.Lore";
    }

    public static class Factory<I> implements ItemProcessorFactory<I> {
        @Override
        public ItemProcessor<I> create(Object arg) {
            LoreProcessor<I> lore = LoreProcessor.createLoreModifier(arg);
            return new OverwritableLoreProcessor<>(lore);
        }
    }

    @Override
    public Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
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
}
