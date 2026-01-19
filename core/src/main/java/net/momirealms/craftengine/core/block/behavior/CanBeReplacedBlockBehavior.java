package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;

public interface CanBeReplacedBlockBehavior {

    boolean canBeReplaced(BlockPlaceContext context, ImmutableBlockState state);
}
