package net.momirealms.craftengine.bukkit.plugin.network.handler;

import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureSnapshotState;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.network.EntityPacketHandler;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public final class FurniturePacketHandler implements EntityPacketHandler {
    public final Furniture furniture;
    // 保留本次开始追踪时的快照，供停止追踪时清理旧实体并配对行为回调。
    public final FurnitureSnapshotState snapshotState;

    public FurniturePacketHandler(Furniture furniture) {
        this.furniture = furniture;
        this.snapshotState = furniture.snapshotState();
    }

    @Override
    public boolean handleEntitiesRemove(NetWorkUser user, IntList entityIds) {
        Player player = (Player) user;
        player.removeTrackedEntity(this.furniture.entityId());
        FurnitureSnapshotState currentSnapshot = this.furniture.snapshotState();
        currentSnapshot.hide(player);
        // 变体切换、旋转会替换快照；旧快照的显示包可能晚于切换发送，不能只清理当前实体。
        // 未替换快照时只隐藏一次，避免正常停止追踪重复发送移除包。
        if (currentSnapshot != this.snapshotState) {
            this.snapshotState.hide(player);
        }
        this.furniture.controller.onAsyncPlayerUntrack(player, this.snapshotState);
        return true;
    }

    @Override
    public void handleSyncEntityPosition(NetWorkUser user, ByteBufPacketEvent event, int entityId, FriendlyByteBuf buf) {
        event.setCancelled(true);
    }

    @Override
    public void handleMove(NetWorkUser user, ByteBufPacketEvent event, int entityId, FriendlyByteBuf buf) {
        event.setCancelled(true);
    }
}
