package net.momirealms.craftengine.core.item.updater;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public interface ItemUpdaterFactory<T extends ItemUpdater> {

    T create(Key item, Map<String, Object> args);
}
