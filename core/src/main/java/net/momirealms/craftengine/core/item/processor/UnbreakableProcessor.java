package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

public final class UnbreakableProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<UnbreakableProcessor> FACTORY = new Factory();
    private static final Object[] NBT_PATH = new Object[]{"Unbreakable"};
    private final boolean argument;

    public UnbreakableProcessor(boolean argument) {
        this.argument = argument;
    }

    public boolean unbreakable() {
        return argument;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.unbreakable(this.argument);
        return item;
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.UNBREAKABLE;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "Unbreakable";
    }

    private static class Factory implements ItemProcessorFactory<UnbreakableProcessor> {

        @Override
        public UnbreakableProcessor create(Object arg) {
            boolean value = ResourceConfigUtils.getAsBoolean(arg, "unbreakable");
            return new UnbreakableProcessor(value);
        }
    }
}
