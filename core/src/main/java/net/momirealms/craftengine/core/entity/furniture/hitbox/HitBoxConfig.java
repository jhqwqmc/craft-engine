package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.util.Key;
import org.joml.Vector3f;

public interface HitBoxConfig {

    Key type();

    SeatConfig[] seats();

    Vector3f position();

    boolean blocksBuilding();

    boolean canBeHitByProjectile();

    boolean canUseItemOn();
}
