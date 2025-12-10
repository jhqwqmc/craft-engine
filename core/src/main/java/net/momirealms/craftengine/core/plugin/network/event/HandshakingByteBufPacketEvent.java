package net.momirealms.craftengine.core.plugin.network.event;

import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class HandshakingByteBufPacketEvent extends ByteBufPacketEvent {

    public HandshakingByteBufPacketEvent(int packetID, FriendlyByteBuf buf, int preIndex, PacketFlow direction) {
        super(packetID, buf, preIndex, ConnectionState.HANDSHAKING, direction);
    }
}
