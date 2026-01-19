package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.CustomBlock;

import java.util.Map;

public interface BlockBehaviorFactory<T extends BlockBehavior> {

    T create(CustomBlock block, Map<String, Object> arguments);
}
