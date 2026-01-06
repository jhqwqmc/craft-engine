package net.momirealms.craftengine.bukkit.block.entity.renderer;

import com.google.common.cache.Cache;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.block.entity.DisplayItemBlockEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.UUID;

public class DynamicItemFrameRenderer implements DynamicBlockEntityRenderer {
    public final DisplayItemBlockEntity blockEntity;
    public final Config config;
    public final Object cachedSpawnPacket;
    public final Object cachedDespawnPacket;
    public final int entityId;

    public DynamicItemFrameRenderer(DisplayItemBlockEntity blockEntity, BlockPos pos) {
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
            Cache<Object, Boolean> receivedMapData = player.receivedMapData();
            Object received = receivedMapData.getIfPresent(savedData);
            if (received != null) return; // 节约带宽静态渲染
            receivedMapData.put(savedData, Boolean.TRUE); // 存入用于标记的单例对象
            Object vanillaRender = CoreReflections.methodHandle$MapItemSavedData$vanillaRenderGetter.invokeExact(savedData);
            byte[] buffer = FastNMS.INSTANCE.field$RenderData$buffer(vanillaRender);
            Object patch = FastNMS.INSTANCE.constructor$MapItemSavedData$MapPatch(0, 0, 128, 128, buffer);
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
}
