package net.momirealms.craftengine.core.block.entity;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public abstract class BlockEntity {
    protected final BlockPos pos;
    protected ImmutableBlockState blockState;
    protected BlockEntityType<? extends BlockEntity> type;
    public CEWorld world;
    protected boolean valid;
    @Nullable
    protected DynamicBlockEntityRenderer blockEntityRenderer;

    protected BlockEntity(BlockEntityType<? extends BlockEntity> type, BlockPos pos, ImmutableBlockState blockState) {
        this.pos = pos;
        this.blockState = blockState;
        this.type = type;
    }

    public final CompoundTag saveAsTag() {
        CompoundTag tag = new CompoundTag();
        this.saveId(tag);
        this.savePos(tag);
        this.saveCustomData(tag);
        return tag;
    }

    private void saveId(CompoundTag tag) {
        tag.putString("id", this.type.id().asString());
    }

    public void setBlockState(ImmutableBlockState blockState) {
        this.blockState = blockState;
    }

    public ImmutableBlockState blockState() {
        return blockState;
    }

    public CEWorld world() {
        return world;
    }

    public void setWorld(CEWorld world) {
        this.world = world;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private void savePos(CompoundTag tag) {
        tag.putInt("x", this.pos.x());
        tag.putInt("y", this.pos.y());
        tag.putInt("z", this.pos.z());
    }

    protected void saveCustomData(CompoundTag tag) {
    }

    public void loadCustomData(CompoundTag tag) {
    }

    public void preRemove() {
    }

    public static BlockPos readPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public BlockEntityType<? extends BlockEntity> type() {
        return this.type;
    }

    public @Nullable DynamicBlockEntityRenderer blockEntityRenderer() {
        return blockEntityRenderer;
    }

    public static BlockPos readPosAndVerify(CompoundTag tag, ChunkPos chunkPos) {
        int x = tag.getInt("x", 0);
        int y = tag.getInt("y", 0);
        int z = tag.getInt("z", 0);
        int sectionX = SectionPos.blockToSectionCoord(x);
        int sectionZ = SectionPos.blockToSectionCoord(z);
        if (sectionX != chunkPos.x || sectionZ != chunkPos.z) {
            x = chunkPos.x * 16 + SectionPos.sectionRelative(x);
            z = chunkPos.z * 16 + SectionPos.sectionRelative(z);
        }
        return new BlockPos(x, y, z);
    }

    public BlockPos pos() {
        return this.pos;
    }

    public boolean isValidBlockState(ImmutableBlockState blockState) {
        return this.type == blockState.blockEntityType();
    }

    public interface Factory<T extends BlockEntity> {

        T create(BlockPos pos, ImmutableBlockState state);
    }
}
