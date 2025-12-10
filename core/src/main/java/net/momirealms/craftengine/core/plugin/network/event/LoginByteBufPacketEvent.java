package net.momirealms.craftengine.core.plugin.network.event;

import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class LoginByteBufPacketEvent extends ByteBufPacketEvent {

    public LoginByteBufPacketEvent(int packetID, FriendlyByteBuf buf, int preIndex, PacketFlow direction) {
        super(packetID, buf, preIndex, ConnectionState.LOGIN, direction);
    }
}
