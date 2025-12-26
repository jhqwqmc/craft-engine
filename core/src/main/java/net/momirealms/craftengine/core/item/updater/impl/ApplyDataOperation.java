package net.momirealms.craftengine.core.item.updater.impl;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessors;
import net.momirealms.craftengine.core.item.updater.ItemUpdater;
import net.momirealms.craftengine.core.item.updater.ItemUpdaterType;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplyDataOperation<I> implements ItemUpdater<I> {
    public static final Type<?> TYPE = new Type<>();
    private final List<ItemProcessor<I>> modifiers;

    public ApplyDataOperation(List<ItemProcessor<I>> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public Item<I> update(Item<I> item, ItemBuildContext context) {
        if (this.modifiers != null) {
            for (ItemProcessor<I> modifier : this.modifiers) {
                modifier.apply(item, context);
            }
        }
        return item;
    }

    public static class Type<I> implements ItemUpdaterType<I> {

        @SuppressWarnings("unchecked")
        @Override
        public ItemUpdater<I> create(Key item, Map<String, Object> args) {
            List<ItemProcessor<I>> modifiers = new ArrayList<>();
            Map<String, Object> data = ResourceConfigUtils.getAsMap(args.get("data"), "data");
            ItemProcessors.applyDataModifiers(data, m -> {
                modifiers.add((ItemProcessor<I>) m);
            });
            return new ApplyDataOperation<>(modifiers);
        }
    }
}
