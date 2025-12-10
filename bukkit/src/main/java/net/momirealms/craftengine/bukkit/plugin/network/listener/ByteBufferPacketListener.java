package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;

public interface ByteBufferPacketListener {

    default void onPacketReceive(NetWorkUser user, ByteBufPacketEvent event) {
    }

    default void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
    }
}
