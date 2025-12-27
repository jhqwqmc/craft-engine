package net.momirealms.craftengine.core.item.updater.impl;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.updater.ItemUpdater;
import net.momirealms.craftengine.core.item.updater.ItemUpdaterFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class TransmuteOperation<I> implements ItemUpdater<I> {
    public static final ItemUpdaterFactory<?> FACTORY = new Factory<>();
    private final Key newMaterial;

    public TransmuteOperation(Key newMaterial) {
        this.newMaterial = newMaterial;
    }

    @Override
    public Item<I> update(Item<I> item, ItemBuildContext context) {
        return item.transmuteCopy(this.newMaterial, item.count());
    }

    private static class Factory<I> implements ItemUpdaterFactory<I> {

        @Override
        public ItemUpdater<I> create(Key item, Map<String, Object> args) {
            return new TransmuteOperation<>(Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(args.get("material"), "warning.config.item.updater.transmute.missing_material")));
        }
    }
}
