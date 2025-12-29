package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

public final class OverwritableCustomModelDataProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<OverwritableCustomModelDataProcessor> FACTORY = new Factory();
    private final int argument;

    public OverwritableCustomModelDataProcessor(int argument) {
        this.argument = argument;
    }

    public int customModelData() {
        return this.argument;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (item.customModelData().isPresent()) return item;
        item.customModelData(this.argument);
        return item;
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.CUSTOM_MODEL_DATA;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"CustomModelData"};
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "CustomModelData";
    }

    private static class Factory implements ItemProcessorFactory<OverwritableCustomModelDataProcessor> {

        @Override
        public OverwritableCustomModelDataProcessor create(Object arg) {
            int customModelData = ResourceConfigUtils.getAsInt(arg, "custom-model-data");
            return new OverwritableCustomModelDataProcessor(customModelData);
        }
    }
}
