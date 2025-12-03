package net.momirealms.craftengine.core.block.behavior;

import java.util.concurrent.Callable;

public interface IsPathFindableBlockBehavior {

    // 1.20-1.20.4 BlockState state, BlockGetter world, BlockPos pos, PathComputationType type
    // 1.20.5+ BlockState state, PathComputationType pathComputationType
    boolean isPathFindable(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception;
}
