package net.momirealms.craftengine.core.block.entity.tick;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;

public class TickingBlockEntityImpl<T extends BlockEntity> implements TickingBlockEntity {
    private final T blockEntity;
    private final BlockEntityTicker<T> ticker;
    private final CEChunk chunk;

    public TickingBlockEntityImpl(CEChunk chunk, T blockEntity, BlockEntityTicker<T> ticker) {
        this.blockEntity = blockEntity;
        this.ticker = ticker;
        this.chunk = chunk;
    }

    @Override
    public BlockPos pos() {
        return this.blockEntity.pos();
    }

    @Override
    public void tick() {
        // 还没加载完全
        if (this.blockEntity.world == null) return;
        BlockPos pos = pos();
        ImmutableBlockState state = this.chunk.getBlockState(pos);
        // 不是合法方块
        if (!this.blockEntity.isValidBlockState(state)) {
            this.chunk.removeBlockEntity(pos);
            Debugger.BLOCK_ENTITY.warn(() -> "Invalid block entity(" + this.blockEntity.getClass().getSimpleName() + ") with state " + state + " found at world " + this.chunk.world().name() + " " + pos, null);
            return;
        }
        try {
            this.ticker.tick(this.chunk.world(), pos, state, this.blockEntity);
        } catch (Throwable t) {
            CraftEngine.instance().logger().warn("Failed to tick block entity(" + this.blockEntity.getClass().getSimpleName() + ") at world " + this.chunk.world().name() + " " + pos, t);
        }
    }

    @Override
    public boolean isValid() {
        return this.blockEntity.isValid();
    }
}
