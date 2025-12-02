package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import org.joml.Vector3f;

public interface FurnitureHitBoxConfig<H extends FurnitureHitBox> {

    H create(Furniture furniture);

    SeatConfig[] seats();

    Vector3f position();

    boolean blocksBuilding();

    boolean canBeHitByProjectile();

    boolean canUseItemOn();
}
