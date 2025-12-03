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
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.PayloadChannelKeys;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.ResourceKey;

public class PayloadHelper {
    public static final byte[] JADE_RESPONSE = new byte[]{0, 0, 0, 0};

    public static void registerDataTypes() {
        registerDataType(ClientCustomBlockPacket.TYPE, ClientCustomBlockPacket.CODEC);
        registerDataType(CancelBlockUpdatePacket.TYPE, CancelBlockUpdatePacket.CODEC);
        registerDataType(ClientBlockStateSizePacket.TYPE, ClientBlockStateSizePacket.CODEC);
        registerDataType(VisualBlockStatePacket.TYPE, VisualBlockStatePacket.CODEC);
    }

    public static <T extends ModPacket> void registerDataType(ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> key, NetworkCodec<FriendlyByteBuf, T> codec) {
        ((WritableRegistry<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>>) BuiltInRegistries.MOD_PACKET).register(key, codec);
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
