package net.momirealms.craftengine.bukkit.item;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBuiltInRegistries;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;

public final class DataComponentPredicateTypes {
    private DataComponentPredicateTypes() {}

    public static Object byId(Key key) {
        if (!VersionHelper.isOrAbove1_21_5()) return null;
        return FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, KeyUtils.toResourceLocation(key));
    }
}
