package net.momirealms.craftengine.bukkit.block.entity.renderer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.block.entity.ItemDisplayBlockEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.MutableBoolean;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DynamicItemFrameRenderer implements DynamicBlockEntityRenderer {
    public static final Cache<Object, Cache<Player, MutableBoolean>> MAP_DATA_CACHE = CacheBuilder.newBuilder()
            .weakKeys()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .concurrencyLevel(4)
            .build();
    public final ItemDisplayBlockEntity blockEntity;
    public final Config config;
    public final Object cachedSpawnPacket;
    public final Object cachedDespawnPacket;
    public final int entityId;

    public DynamicItemFrameRenderer(ItemDisplayBlockEntity blockEntity, BlockPos pos) {
        this.entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        this.blockEntity = blockEntity;
        this.config = blockEntity.config;
        Vector3f position = this.config.position;
        Direction direction = blockEntity.blockState().get(blockEntity.behavior.directionProperty);
        this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                this.entityId, UUID.randomUUID(), pos.x() + position.x, pos.y() + position.y, pos.z() + position.z,
                0, 0, this.config.isGlow ? MEntityTypes.GLOW_ITEM_FRAME : MEntityTypes.ITEM_FRAME, direction.ordinal(), CoreReflections.instance$Vec3$Zero, 0
        );
        this.cachedDespawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(IntList.of(entityId));
    }

    @Override
    public void show(Player player) {
        player.sendPacket(this.cachedSpawnPacket, false);
        update(player);
    }

    @Override
    public void hide(Player player) {
        player.sendPacket(this.cachedDespawnPacket, false);
    }

    @Override
    public void update(Player player) {
        player.sendPacket(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(this.entityId, this.blockEntity.cacheMetadata()), false);
        if (this.config.renderMapItem) {
            updateMapItem(player);
        }
    }

    private void updateMapItem(Player player) {
        if (player.isFakePlayer()) return;
        Object mapId = this.blockEntity.mapId();
        if (mapId == null) return;
        CEWorld world = this.blockEntity.world;
        if (world == null) return;
        Object savedData = this.blockEntity.mapItemSavedData();
        if (savedData == null) {
            savedData = FastNMS.INSTANCE.method$MapItem$getSavedData(mapId, world.world.serverWorld());
            if (savedData == null) return;
            this.blockEntity.mapItemSavedData(savedData);
        }
        try {
            Cache<Player, MutableBoolean> savedDataCache = MAP_DATA_CACHE.get(savedData, () -> CacheBuilder.newBuilder().weakKeys().expireAfterAccess(5, TimeUnit.MINUTES).concurrencyLevel(4).build());
            MutableBoolean sent = savedDataCache.get(player, () -> new MutableBoolean(false));
            if (sent.booleanValue()) return;
            sent.set(true);
            Object vanillaRender = CoreReflections.methodHandle$MapItemSavedData$vanillaRenderGetter.invokeExact(savedData);
            byte[] buffer = FastNMS.INSTANCE.field$RenderData$buffer(vanillaRender);
            Object patch = createPatch(buffer);
            byte scale = FastNMS.INSTANCE.field$MapItemSavedData$scale(savedData);
            boolean locked = FastNMS.INSTANCE.field$MapItemSavedData$locked(savedData);
            Object packet = FastNMS.INSTANCE.constructor$ClientboundMapItemDataPacket(mapId, scale, locked, null, patch);
            player.sendPacket(packet, false);
        } catch (Throwable e) {
            CraftEngine.instance().logger().warn("Cannot update map item for player " + player.name(), e);
        }
    }

    public record Config(Vector3f position, boolean isGlow, boolean invisible, boolean renderMapItem) {
    }

    private static Object createPatch(byte[] buffer) {
        byte[] mapColors = new byte[128 * 128];
        for (int row = 0; row < 128; row++) {
            for (int col = 0; col < 128; col++) {
                mapColors[row + col * 128] = buffer[row + col * 128];
            }
        }
        return FastNMS.INSTANCE.constructor$MapItemSavedData$MapPatch(0, 0, 128, 128, mapColors);
    }
}
