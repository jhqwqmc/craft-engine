package net.momirealms.craftengine.core.world.chunk.storage;

import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface WorldDataStorage {

    default CEChunk readNewChunkAt(CEWorld world, ChunkPos pos) throws IOException {
        this.clearChunkAt(pos);
        return this.readChunkAt(world, pos);
    }

    @NotNull
    CEChunk readChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos) throws IOException;

    void writeChunkAt(@NotNull ChunkPos pos, @NotNull CEChunk chunk) throws IOException;

    void clearChunkAt(@NotNull ChunkPos pos) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;
}
