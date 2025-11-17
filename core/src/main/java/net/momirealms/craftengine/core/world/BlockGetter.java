package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.block.BlockStateWrapper;

public interface BlockGetter {

    BlockStateWrapper getBlockState(int x, int y, int z);

    default BlockStateWrapper getBlockState(BlockPos pos) {
        return getBlockState(pos.x, pos.y, pos.z);
    }
}
