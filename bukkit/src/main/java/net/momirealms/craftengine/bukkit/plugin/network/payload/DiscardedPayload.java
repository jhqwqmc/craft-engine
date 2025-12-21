package net.momirealms.craftengine.bukkit.plugin.network.payload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public record DiscardedPayload(Key channel, Object rawPayload) implements Payload {
    public static final boolean useNewMethod = NetworkReflections.method$DiscardedPayload$data == null;

    public static DiscardedPayload from(Object payload) {
        try {
            Object type = NetworkReflections.method$CustomPacketPayload$type.invoke(payload);
            Object id = NetworkReflections.method$CustomPacketPayload$Type$id.invoke(type);
            Key channel = Key.of(id.toString());
            return new DiscardedPayload(channel, payload);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to create DiscardedPayload", e);
            return null;
        }
    }

    public ByteBuf getData() {
        try {
            if (useNewMethod) {
                return Unpooled.wrappedBuffer((byte[]) NetworkReflections.method$DiscardedPayload$dataByteArray.invoke(this.rawPayload()));
            } else {
                return (ByteBuf) NetworkReflections.method$DiscardedPayload$data.invoke(this.rawPayload());
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to get data from DiscardedPayload", e);
            return Unpooled.EMPTY_BUFFER;
        }
    }

    public FriendlyByteBuf toBuffer() {
        return new FriendlyByteBuf(this.getData());
    }
}
