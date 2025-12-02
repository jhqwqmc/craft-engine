package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.AbstractFurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfigFactory;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.joml.Vector3f;

import java.util.Map;
import java.util.function.Consumer;

public class InteractionFurnitureHitboxConfig extends AbstractFurnitureHitBoxConfig<InteractionFurnitureHitbox> {
    public static final Factory FACTORY = new Factory();
    public static final InteractionFurnitureHitboxConfig DEFAULT =  new InteractionFurnitureHitboxConfig(new SeatConfig[0], new Vector3f(), false, false, false, false, new Vector3f(1,1,1), true);

    public final Vector3f size;
    public final boolean responsive;
    public final boolean invisible;

    public InteractionFurnitureHitboxConfig(SeatConfig[] seats,
                                            Vector3f position,
                                            boolean canUseItemOn,
                                            boolean blocksBuilding,
                                            boolean canBeHitByProjectile,
                                            boolean invisible,
                                            Vector3f size,
                                            boolean responsive) {
        super(seats, position, canUseItemOn, blocksBuilding, canBeHitByProjectile);
        this.size = size;
        this.responsive = responsive;
        this.invisible = invisible;
    }

    public Vector3f size() {
        return size;
    }

    public boolean responsive() {
        return responsive;
    }

    public boolean invisible() {
        return invisible;
    }

    @Override
    public void prepareForPlacement(WorldPosition targetPos, Consumer<AABB> aabbConsumer) {
        if (this.blocksBuilding) {
            Vec3d relativePosition = Furniture.getRelativePosition(targetPos, this.position);
            aabbConsumer.accept(AABB.fromInteraction(relativePosition, size.x, size.y));
        }
    }

    @Override
    public InteractionFurnitureHitbox create(Furniture furniture) {
        return new InteractionFurnitureHitbox(furniture, this);
    }

    public static class Factory implements FurnitureHitBoxConfigFactory<InteractionFurnitureHitbox> {

        @Override
        public InteractionFurnitureHitboxConfig create(Map<String, Object> arguments) {
            Vector3f position = ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0), "position");
            float width;
            float height;
            if (arguments.containsKey("scale")) {
                String[] split = arguments.get("scale").toString().split(",");
                width = Float.parseFloat(split[0]);
                height = Float.parseFloat(split[1]);
            } else {
                width = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("width", 1), "width");
                height = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("height", 1), "height");
            }
            boolean canUseOn = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("can-use-item-on", false), "can-use-item-on");
            boolean interactive = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("interactive", true), "interactive");
            boolean canBeHitByProjectile = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("can-be-hit-by-projectile", false), "can-be-hit-by-projectile");
            boolean blocksBuilding = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("blocks-building", true), "blocks-building");
            boolean invisible = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("invisible", false), "invisible");
            return new InteractionFurnitureHitboxConfig(
                    SeatConfig.fromObj(arguments.get("seats")),
                    position, canUseOn, blocksBuilding, canBeHitByProjectile, invisible,
                    new Vector3f(width, height, width),
                    interactive
            );
        }
    }
}
