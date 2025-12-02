package net.momirealms.craftengine.bukkit.plugin.network.handler;

import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.network.EntityPacketHandler;
import net.momirealms.craftengine.core.plugin.network.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;

public class FurniturePacketHandler implements EntityPacketHandler {
    private final int metaEntityId;
    private final int[] virtualHitboxEntities;

    public FurniturePacketHandler(int metaEntityId, int[] virtualHitboxEntities) {
        this.virtualHitboxEntities = virtualHitboxEntities;
        this.metaEntityId = metaEntityId;
    }

    @Override
    public boolean handleEntitiesRemove(NetWorkUser user, IntList entityIds) {
        ((Player) user).removeTrackedFurniture(this.metaEntityId);
        for (int entityId : this.virtualHitboxEntities) {
            entityIds.add(entityId);
        }
        return true;
    }

    @Override
    public void handleSyncEntityPosition(NetWorkUser user, NMSPacketEvent event, Object packet) {
        event.setCancelled(true);
    }

    @Override
    public void handleMove(NetWorkUser user, NMSPacketEvent event, Object packet) {
        event.setCancelled(true);
    }

    @Override
    public void handleMoveAndRotate(NetWorkUser user, NMSPacketEvent event, Object packet) {
        event.setCancelled(true);
    }
}
