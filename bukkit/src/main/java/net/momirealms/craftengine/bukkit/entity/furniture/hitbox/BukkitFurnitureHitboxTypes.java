package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxTypes;

public class BukkitFurnitureHitboxTypes extends FurnitureHitBoxTypes {

    public static void init() {}

    static {
        register(INTERACTION, InteractionFurnitureHitboxConfig.FACTORY);
//        register(SHULKER, ShulkerFurnitureHitboxConfig.FACTORY);
//        register(HAPPY_GHAST, HappyGhastFurnitureHitboxConfig.FACTORY);
//        register(CUSTOM, CustomFurnitureHitboxConfig.FACTORY);
    }
}
