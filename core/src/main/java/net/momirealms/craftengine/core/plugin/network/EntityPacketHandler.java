package net.momirealms.craftengine.core.plugin.network;

import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.core.entity.player.Player;

public interface EntityPacketHandler {

    default boolean handleEntitiesRemove(NetWorkUser user, IntList entityIds) {
        return false;
    }

    default void handleSetEntityData(Player user, ByteBufPacketEvent event) {
    }

    default void handleSyncEntityPosition(NetWorkUser user, NMSPacketEvent event, Object packet) {
    }

    default void handleMoveAndRotate(NetWorkUser user, NMSPacketEvent event, Object packet) {
    }

    default void handleMove(NetWorkUser user, NMSPacketEvent event, Object packet) {
    }
}
