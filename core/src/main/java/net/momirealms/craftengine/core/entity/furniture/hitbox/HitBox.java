package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.entity.seat.SeatOwner;
import net.momirealms.craftengine.core.world.EntityHitResult;
import net.momirealms.craftengine.core.world.Vec3d;

import java.util.Optional;

public interface HitBox extends SeatOwner {

    Seat<HitBox>[] seats();

    Optional<EntityHitResult> clip(Vec3d min, Vec3d max);

    HitBoxPart[] parts();

    HitBoxConfig config();
}
