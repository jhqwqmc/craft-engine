package net.momirealms.craftengine.core.block.behavior.special;

import java.util.concurrent.Callable;

public interface FallOnBlockBehavior {

    // 1.20.1~1.21.4 Level world, BlockState state, BlockPos pos, Entity entity, float fallDistance
    // 1.21.5+ Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance
    default void fallOn(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    // BlockGetter level, Entity entity
    default void updateEntityMovementAfterFallOn(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }
}
