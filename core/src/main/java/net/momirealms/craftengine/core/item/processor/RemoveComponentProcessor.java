package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.NetworkItemHandler;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.Collections;
import java.util.List;

public class RemoveComponentProcessor<I> implements ItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final List<String> arguments;

    public RemoveComponentProcessor(List<String> arguments) {
        this.arguments = arguments;
    }

    public List<String> components() {
        return Collections.unmodifiableList(this.arguments);
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        for (String argument : this.arguments) {
            item.removeComponent(argument);
        }
        return item;
    }

    @Override
    public Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        for (String component : this.arguments) {
            Tag previous = item.getSparrowNBTComponent(component);
            if (previous != null) {
                networkData.put(component, NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
            }
        }
        return item;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            List<String> data = MiscUtils.getAsStringList(arg);
            return new RemoveComponentProcessor<>(data);
        }
    }
}
