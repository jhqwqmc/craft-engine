package net.momirealms.craftengine.core.item.behavior;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public class EmptyItemBehavior extends ItemBehavior {
    public static final ItemBehaviorFactory<EmptyItemBehavior> FACTORY = new Factory();
    public static final EmptyItemBehavior INSTANCE = new EmptyItemBehavior();

    private EmptyItemBehavior() {}

    private static class Factory implements ItemBehaviorFactory<EmptyItemBehavior> {

        @Override
        public EmptyItemBehavior create(Pack pack, Path path, String node, Key id, Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
