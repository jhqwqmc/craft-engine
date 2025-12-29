package net.momirealms.craftengine.core.item.updater;

import net.momirealms.craftengine.core.item.updater.impl.ApplyDataOperation;
import net.momirealms.craftengine.core.item.updater.impl.ResetOperation;
import net.momirealms.craftengine.core.item.updater.impl.TransmuteOperation;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class ItemUpdaters {
    public static final ItemUpdaterType<ApplyDataOperation> APPLY_DATA = register(Key.ce("apply_data"), ApplyDataOperation.FACTORY);
    public static final ItemUpdaterType<TransmuteOperation> TRANSMUTE = register(Key.ce("transmute"), TransmuteOperation.FACTORY);
    public static final ItemUpdaterType<ResetOperation> RESET = register(Key.ce("reset"), ResetOperation.FACTORY);

    private ItemUpdaters() {}

    public static <T extends ItemUpdater> ItemUpdaterType<T> register(Key id, ItemUpdaterFactory<T> factory) {
        ItemUpdaterType<T> type = new ItemUpdaterType<>(id, factory);
        ((WritableRegistry<ItemUpdaterType<?>>) BuiltInRegistries.ITEM_UPDATER_TYPE)
                .register(ResourceKey.create(Registries.ITEM_UPDATER_TYPE.location(), id), type);
        return type;
    }

    public static ItemUpdater fromMap(Key item, Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.item.updater.missing_type");
        Key key = Key.ce(type);
        ItemUpdaterType<? extends ItemUpdater> updaterType = BuiltInRegistries.ITEM_UPDATER_TYPE.getValue(key);
        if (updaterType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.updater.invalid_type", type);
        }
        return updaterType.factory().create(item, map);
    }
}
