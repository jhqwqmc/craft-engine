package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;

import java.util.Map;

// todo refactor this on 1.0
public interface BlockBehaviorFactory {

    BlockBehavior create(CustomBlock block, Map<String, Object> arguments);
}
