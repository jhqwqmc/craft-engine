package net.momirealms.craftengine.bukkit.plugin.network.payload;

import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.network.payload.protocol.CancelBlockUpdatePacket;
import net.momirealms.craftengine.bukkit.plugin.network.payload.protocol.ClientBlockStateSizePacket;
import net.momirealms.craftengine.bukkit.plugin.network.payload.protocol.ClientCustomBlockPacket;
import net.momirealms.craftengine.bukkit.plugin.network.payload.protocol.VisualBlockStatePacket;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.plugin.network.ModPacket;
import net.momirealms.craftengine.core.plugin.network.ModPacketType;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.PayloadChannelKeys;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.ResourceKey;

public final class PayloadHelper {
    public static final ModPacketType<ClientCustomBlockPacket> CLIENT_CUSTOM_BLOCK = register(ClientCustomBlockPacket.TYPE, ClientCustomBlockPacket.CODEC);
    public static final ModPacketType<CancelBlockUpdatePacket> CANCEL_BLOCK_UPDATE = register(CancelBlockUpdatePacket.TYPE, CancelBlockUpdatePacket.CODEC);
    public static final ModPacketType<ClientBlockStateSizePacket> CLIENT_BLOCK_STATE_SIZE = register(ClientBlockStateSizePacket.TYPE, ClientBlockStateSizePacket.CODEC);
    public static final ModPacketType<VisualBlockStatePacket> VISUAL_BLOCK_STATE = register(VisualBlockStatePacket.TYPE, VisualBlockStatePacket.CODEC);

    private PayloadHelper() {}

    public static void init() {
    }

    public static <T extends ModPacket> ModPacketType<T> register(ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> key, NetworkCodec<FriendlyByteBuf, T> codec) {
        ModPacketType<T> type = new ModPacketType<>(key.location(), codec);
        ((WritableRegistry<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>>) BuiltInRegistries.MOD_PACKET).register(key, codec);
        return type;
    }

    public static void sendData(NetWorkUser user, ModPacket data) {
        @SuppressWarnings("unchecked")
        NetworkCodec<FriendlyByteBuf, ModPacket> codec = (NetworkCodec<FriendlyByteBuf, ModPacket>) BuiltInRegistries.MOD_PACKET.getValue(data.type());
        if (codec == null) {
            CraftEngine.instance().logger().warn("Unknown data type class: " + data.getClass().getName());
            return;
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(BuiltInRegistries.MOD_PACKET.getId(codec));
        codec.encode(buf, data);
        user.sendCustomPayload(PayloadChannelKeys.CRAFTENGINE_CHANNEL, buf.array());
    }

    public static void handleReceiver(Payload payload, NetWorkUser user) {
        try {
            if (payload.channel().equals(PayloadChannelKeys.CRAFTENGINE_CHANNEL)) {
                handleCraftEngineModReceiver(payload, user);
            }
        } catch (Throwable e) {
            // 乱发包我给你踹了
            user.kick(Component.translatable(
                    "disconnect.craftengine.invalid_payload",
                    "Connection terminated due to transmission of invalid payload. \n Please ensure that the client mod and server plugin are the latest version."
            ));
            Debugger.COMMON.warn(() -> "Failed to handle payload", e);
        }
    }

    private static void handleCraftEngineModReceiver(Payload payload, NetWorkUser user) {
        FriendlyByteBuf buf = payload.toBuffer();
        byte type = buf.readByte();
        @SuppressWarnings("unchecked")
        NetworkCodec<FriendlyByteBuf, ModPacket> codec = (NetworkCodec<FriendlyByteBuf, ModPacket>) BuiltInRegistries.MOD_PACKET.getValue(type);
        if (codec == null) {
            Debugger.COMMON.debug(() -> "Unknown data type received: " + type);
            return;
        }

        ModPacket networkData = codec.decode(buf);
        networkData.handle(user);
    }
}
