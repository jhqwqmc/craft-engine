package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxTypes;
import net.momirealms.craftengine.core.util.VersionHelper;

public class BukkitFurnitureHitboxTypes extends FurnitureHitBoxTypes {

    public static void init() {}

    static {
        register(INTERACTION, InteractionFurnitureHitboxConfig.FACTORY);
        register(SHULKER, ShulkerFurnitureHitboxConfig.FACTORY);
         register(CUSTOM, CustomFurnitureHitboxConfig.FACTORY);
        if (VersionHelper.isOrAbove1_21_6()) {
            register(HAPPY_GHAST, HappyGhastFurnitureHitboxConfig.FACTORY);
        }
    }
}
