package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.entity.seat.SeatOwner;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.jetbrains.annotations.Nullable;

public interface FurnitureHitBox extends SeatOwner {

    Seat<FurnitureHitBox>[] seats();

    AABB aabb();

    Vec3d position();

    @Nullable
    Collider collider();

    int[] virtualEntityIds();

    void show(Player player);

    void hide(Player player);

    FurnitureHitBoxConfig<?> config();
}
