package net.momirealms.craftengine.core.item.updater.impl;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessors;
import net.momirealms.craftengine.core.item.updater.ItemUpdater;
import net.momirealms.craftengine.core.item.updater.ItemUpdaterFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ApplyDataOperation implements ItemUpdater {
    public static final ItemUpdaterFactory<ApplyDataOperation> FACTORY = new Factory();
    private final List<ItemProcessor> modifiers;

    public ApplyDataOperation(List<ItemProcessor> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public <I> Item<I> update(Item<I> item, ItemBuildContext context) {
        if (this.modifiers != null) {
            for (ItemProcessor<I> modifier : this.modifiers) {
                modifier.apply(item, context);
            }
        }
        return item;
    }

    private static class Factory implements ItemUpdaterFactory<ApplyDataOperation> {

        @SuppressWarnings("unchecked")
        @Override
        public ApplyDataOperation create(Key item, Map<String, Object> args) {
            List<ItemProcessor> modifiers = new ArrayList<>();
            Map<String, Object> data = ResourceConfigUtils.getAsMap(args.get("data"), "data");
            ItemProcessors.applyDataModifiers(data, m -> {
                modifiers.add(m);
            });
            return new ApplyDataOperation(modifiers);
        }
    }
}
