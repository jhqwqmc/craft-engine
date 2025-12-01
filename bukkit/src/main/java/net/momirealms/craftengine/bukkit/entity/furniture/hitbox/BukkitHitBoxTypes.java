package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.hitbox.HitBoxTypes;

public class BukkitHitBoxTypes extends HitBoxTypes {

    public static void init() {}

    static {
        register(INTERACTION, InteractionHitBoxConfig.FACTORY);
        register(SHULKER, ShulkerHitBoxConfig.FACTORY);
        register(HAPPY_GHAST, HappyGhastHitBoxConfig.FACTORY);
        register(CUSTOM, CustomHitBoxConfig.FACTORY);
    }
}
