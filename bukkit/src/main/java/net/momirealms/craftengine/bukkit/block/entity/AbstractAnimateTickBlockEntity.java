package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.world.BlockPos;

public abstract class AbstractAnimateTickBlockEntity extends BlockEntity {
    protected int tickCount;

    public AbstractAnimateTickBlockEntity(BlockEntityType<? extends BlockEntity> type, BlockPos pos, ImmutableBlockState blockState) {
        super(type, pos, blockState);
    }
}
