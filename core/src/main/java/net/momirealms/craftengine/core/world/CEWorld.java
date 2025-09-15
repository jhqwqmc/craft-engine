package net.momirealms.craftengine.core.world;

import ca.spottedleaf.concurrentutil.map.ConcurrentLong2ReferenceChainedHashTable;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.tick.TickingBlockEntity;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.util.BlockEntityTickersList;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CEWorld {
    public static final String REGION_DIRECTORY = "craftengine";
    public final World world;
    protected final ConcurrentLong2ReferenceChainedHashTable<CEChunk> loadedChunkMap;
    protected final WorldDataStorage worldDataStorage;
    protected final WorldHeight worldHeightAccessor;
    protected List<SectionPos> pendingLightSections = new ArrayList<>();
    protected final Set<SectionPos> lightSections = ConcurrentHashMap.newKeySet(128);
    protected final BlockEntityTickersList tickingBlockEntities = new BlockEntityTickersList();
    protected final List<TickingBlockEntity> pendingTickingBlockEntities = new ArrayList<>();
    protected volatile boolean isTickingBlockEntities = false;
    protected volatile boolean isUpdatingLights = false;
    protected SchedulerTask syncTickTask;
    protected SchedulerTask asyncTickTask;

    public CEWorld(World world, StorageAdaptor adaptor) {
        this.world = world;
        this.loadedChunkMap = ConcurrentLong2ReferenceChainedHashTable.createWithCapacity(1024, 0.5f);
        this.worldDataStorage = adaptor.adapt(world);
        this.worldHeightAccessor = world.worldHeight();
    }

    public CEWorld(World world, WorldDataStorage dataStorage) {
        this.world = world;
        this.loadedChunkMap = ConcurrentLong2ReferenceChainedHashTable.createWithCapacity(1024, 0.5f);
        this.worldDataStorage = dataStorage;
        this.worldHeightAccessor = world.worldHeight();
    }

    public void setTicking(boolean ticking) {
        if (ticking) {
            if (this.syncTickTask == null || this.syncTickTask.cancelled()) {
                this.syncTickTask = CraftEngine.instance().scheduler().sync().runRepeating(this::syncTick, 1, 1);
            }
            if (this.asyncTickTask == null || this.asyncTickTask.cancelled()) {
                this.asyncTickTask = CraftEngine.instance().scheduler().sync().runAsyncRepeating(this::asyncTick, 1, 1);
            }
        } else {
            if (this.syncTickTask != null && !this.syncTickTask.cancelled()) {
                this.syncTickTask.cancel();
            }
            if (this.asyncTickTask != null && !this.asyncTickTask.cancelled()) {
                this.asyncTickTask.cancel();
            }
        }
    }

    public String name() {
        return this.world.name();
    }

    public UUID uuid() {
        return this.world.uuid();
    }

    public void save() {
        try {
            for (ConcurrentLong2ReferenceChainedHashTable.TableEntry<CEChunk> entry : this.loadedChunkMap.entrySet()) {
                CEChunk chunk = entry.getValue();
                if (chunk.dirty()) {
                    this.worldDataStorage.writeChunkAt(new ChunkPos(entry.getKey()), chunk);
                    chunk.setDirty(false);
                }
            }
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Failed to save world chunks", e);
        }
    }

    public World world() {
        return this.world;
    }

    public boolean isChunkLoaded(final long chunkPos) {
        return loadedChunkMap.containsKey(chunkPos);
    }

    public void addLoadedChunk(CEChunk chunk) {
        this.loadedChunkMap.put(chunk.chunkPos().longKey(), chunk);
    }

    public void removeLoadedChunk(CEChunk chunk) {
        this.loadedChunkMap.remove(chunk.chunkPos().longKey());
    }

    @Nullable
    public CEChunk getChunkAtIfLoaded(long chunkPos) {
        return this.loadedChunkMap.get(chunkPos);
    }

    @Nullable
    public CEChunk getChunkAtIfLoaded(int x, int z) {
        return getChunkAtIfLoaded(ChunkPos.asLong(x, z));
    }

    @Nullable
    public CEChunk getChunkAtIfLoaded(ChunkPos chunkPos) {
        return getChunkAtIfLoaded(chunkPos.longKey);
    }

    @Nullable
    public ImmutableBlockState getBlockStateAtIfLoaded(int x, int y, int z) {
        CEChunk chunk = getChunkAtIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockState(x, y, z);
    }

    @Nullable
    public ImmutableBlockState getBlockStateAtIfLoaded(BlockPos blockPos) {
        CEChunk chunk = getChunkAtIfLoaded(blockPos.x() >> 4, blockPos.z() >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockState(blockPos);
    }

    public boolean setBlockStateAtIfLoaded(BlockPos blockPos, ImmutableBlockState blockState) {
        if (this.worldHeightAccessor.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        CEChunk chunk = getChunkAtIfLoaded(blockPos.x() >> 4, blockPos.z() >> 4);
        if (chunk == null) {
            return false;
        }
        chunk.setBlockState(blockPos, blockState);
        return true;
    }

    @Nullable
    public BlockEntity getBlockEntityAtIfLoaded(BlockPos blockPos) {
        if (this.worldHeightAccessor.isOutsideBuildHeight(blockPos)) {
            return null;
        }
        CEChunk chunk = getChunkAtIfLoaded(blockPos.x() >> 4, blockPos.z() >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockEntity(blockPos, true);
    }

    public WorldDataStorage worldDataStorage() {
        return worldDataStorage;
    }

    public void sectionLightUpdated(Collection<SectionPos> pos) {
        if (this.isUpdatingLights) {
            this.pendingLightSections.addAll(pos);
        } else {
            this.lightSections.addAll(pos);
        }
    }

    public WorldHeight worldHeight() {
        return this.worldHeightAccessor;
    }

    public void syncTick() {
        this.tickBlockEntities();
        if (!Config.asyncLightUpdate()) {
            this.updateLight();
        }
    }

    public void asyncTick() {
        if (Config.asyncLightUpdate()) {
            this.updateLight();
        }
    }

    public abstract void updateLight();

    public void addBlockEntityTicker(TickingBlockEntity ticker) {
        if (this.isTickingBlockEntities) {
            this.pendingTickingBlockEntities.add(ticker);
        } else {
            this.tickingBlockEntities.add(ticker);
        }
    }

    protected void tickBlockEntities() {
        this.isTickingBlockEntities = true;
        if (!this.pendingTickingBlockEntities.isEmpty()) {
            this.tickingBlockEntities.addAll(this.pendingTickingBlockEntities);
            this.pendingTickingBlockEntities.clear();
        }
        if (!this.tickingBlockEntities.isEmpty()) {
            Object[] entities = this.tickingBlockEntities.elements();
            for (int i = 0, size = this.tickingBlockEntities.size(); i < size; i++) {
                TickingBlockEntity entity = (TickingBlockEntity) entities[i];
                if (entity.isValid()) {
                    entity.tick();
                } else {
                    this.tickingBlockEntities.markAsRemoved(i);
                }
            }
            this.tickingBlockEntities.removeMarkedEntries();
        }
        this.isTickingBlockEntities = false;
    }
}
