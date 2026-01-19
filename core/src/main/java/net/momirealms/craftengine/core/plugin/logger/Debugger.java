package net.momirealms.craftengine.core.plugin.logger;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;

import java.util.function.Supplier;

public enum Debugger {
    COMMON(Config::debugCommon),
    PACKET(Config::debugPacket),
    FURNITURE(Config::debugFurniture),
    RESOURCE_PACK(Config::debugResourcePack),
    ITEM(Config::debugItem),
    BLOCK(Config::debugBlock),
    ENTITY_CULLING(Config::debugEntityCulling);

    private final Supplier<Boolean> condition;

    Debugger(Supplier<Boolean> condition) {
        this.condition = condition;
    }

    public void debug(Supplier<String> message) {
        if (this.condition.get()) {
            String s = message.get();
            if (s != null) {
                CraftEngine.instance().logger().info("[DEBUG] " + s);
            }
        }
    }

    public void warn(Supplier<String> message, Throwable e) {
        if (this.condition.get()) {
            String s = message.get();
            if (e != null) {
                if (s != null) {
                    CraftEngine.instance().logger().warn("[DEBUG] " + s, e);
                }
            } else {
                if (s != null) {
                    CraftEngine.instance().logger().warn("[DEBUG] " + s);
                }
            }
        }
    }
}
