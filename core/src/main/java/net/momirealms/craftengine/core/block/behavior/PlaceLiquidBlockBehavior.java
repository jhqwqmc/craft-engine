package net.momirealms.craftengine.core.block.behavior;

import java.util.concurrent.Callable;

public interface PlaceLiquidBlockBehavior {

    boolean placeLiquid(Object thisBlock, Object[] args, Callable<Object> superMethod);

    boolean canPlaceLiquid(Object thisBlock, Object[] args, Callable<Object> superMethod);
}
