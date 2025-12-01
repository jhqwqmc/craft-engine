package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.hitbox.AbstractHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.HitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.HitBoxConfigFactory;
import net.momirealms.craftengine.core.entity.furniture.hitbox.HitBoxTypes;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.joml.Vector3f;

import java.util.Map;

public class CustomHitBoxConfig extends AbstractHitBoxConfig {
    public static final Factory FACTORY = new Factory();
    private final float scale;
    private final EntityType entityType;

    public CustomHitBoxConfig(SeatConfig[] seats,
                              Vector3f position,
                              EntityType type,
                              float scale,
                              boolean blocksBuilding,
                              boolean canBeHitByProjectile) {
        super(seats, position, false, blocksBuilding, canBeHitByProjectile);
        this.scale = scale;
        this.entityType = type;
    }

    public EntityType entityType() {
        return this.entityType;
    }

    public float scale() {
        return this.scale;
    }

    @Override
    public Key type() {
        return HitBoxTypes.CUSTOM;
    }

    public static class Factory implements HitBoxConfigFactory {

        @Override
        public HitBoxConfig create(Map<String, Object> arguments) {
            Vector3f position = ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", "0"), "position");
            float scale = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("scale", 1), "scale");
            String type = (String) arguments.getOrDefault("entity-id", "slime");
            EntityType entityType = Registry.ENTITY_TYPE.get(new NamespacedKey("minecraft", type));
            if (entityType == null) {
                throw new LocalizedResourceConfigException("warning.config.furniture.hitbox.custom.invalid_entity", new IllegalArgumentException("EntityType not found: " + type), type);
            }
            boolean canBeHitByProjectile = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("can-be-hit-by-projectile", false), "can-be-hit-by-projectile");
            boolean blocksBuilding = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("blocks-building", true), "blocks-building");
            return new CustomHitBoxConfig(SeatConfig.fromObj(arguments.get("seats")), position, entityType, scale, blocksBuilding, canBeHitByProjectile);
        }
    }
}
