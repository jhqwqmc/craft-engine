package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxes;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitboxConfigType;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;

public final class BukkitFurnitureHitboxTypes extends FurnitureHitBoxes {
    public static final FurnitureHitboxConfigType<InteractionFurnitureHitbox> INTERACTION = register(Key.ce("interaction"), InteractionFurnitureHitboxConfig.FACTORY);
    public static final FurnitureHitboxConfigType<ShulkerFurnitureHitbox> SHULKER = register(Key.ce("shulker"), ShulkerFurnitureHitboxConfig.FACTORY);
    public static final FurnitureHitboxConfigType<HappyGhastFurnitureHitbox> HAPPY_GHAST = register(Key.ce("happy_ghast"), HappyGhastFurnitureHitboxConfig.FACTORY);
    public static final FurnitureHitboxConfigType<CustomFurnitureHitbox> CUSTOM = register(Key.ce("custom"), CustomFurnitureHitboxConfig.FACTORY);

    public static void init() {}
}
