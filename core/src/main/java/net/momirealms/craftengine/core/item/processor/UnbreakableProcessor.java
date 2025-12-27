package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

public class UnbreakableProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private static final Object[] NBT_PATH = new Object[]{"Unbreakable"};
    private final boolean argument;

    public UnbreakableProcessor(boolean argument) {
        this.argument = argument;
    }

    public boolean unbreakable() {
        return argument;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.unbreakable(this.argument);
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.UNBREAKABLE;
    }

    @Override
    public @Nullable Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "Unbreakable";
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            boolean value = ResourceConfigUtils.getAsBoolean(arg, "unbreakable");
            return new UnbreakableProcessor<>(value);
        }
    }
}
