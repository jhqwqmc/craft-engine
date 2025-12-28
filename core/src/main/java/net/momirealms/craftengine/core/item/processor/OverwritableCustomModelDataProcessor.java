package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

public class OverwritableCustomModelDataProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final int argument;

    public OverwritableCustomModelDataProcessor(int argument) {
        this.argument = argument;
    }

    public int customModelData() {
        return this.argument;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (item.customModelData().isPresent()) return item;
        item.customModelData(this.argument);
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.CUSTOM_MODEL_DATA;
    }

    @Override
    public @Nullable Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"CustomModelData"};
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "CustomModelData";
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor create(Object arg) {
            int customModelData = ResourceConfigUtils.getAsInt(arg, "custom-model-data");
            return new OverwritableCustomModelDataProcessor<>(customModelData);
        }
    }
}
