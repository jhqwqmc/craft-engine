package net.momirealms.craftengine.core.item.updater;

import net.momirealms.craftengine.core.util.Key;

public record ItemUpdaterType<I>(Key id, ItemUpdaterFactory<I> factory) {
}
