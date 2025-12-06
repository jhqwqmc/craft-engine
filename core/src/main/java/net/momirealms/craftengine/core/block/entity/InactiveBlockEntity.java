package net.momirealms.craftengine.core.block.entity;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.sparrow.nbt.CompoundTag;

public class InactiveBlockEntity extends BlockEntity {
    private final CompoundTag tag;

    public InactiveBlockEntity(BlockPos pos,
                               ImmutableBlockState blockState,
                               CompoundTag tag) {
        super(BlockEntityTypes.INACTIVE, pos, blockState);
        this.tag = tag;
    }

    @Override
    public CompoundTag saveAsTag() {
        return this.tag;
    }
}
