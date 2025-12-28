package net.momirealms.craftengine.core.item.behavior;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public interface ItemBehaviorFactory<T extends ItemBehavior> {

    T create(Pack pack, Path path, String node, Key id, Map<String, Object> arguments);
}
