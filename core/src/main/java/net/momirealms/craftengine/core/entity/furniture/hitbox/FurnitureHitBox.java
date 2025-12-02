package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.entity.seat.SeatOwner;
import net.momirealms.craftengine.core.world.EntityHitResult;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface FurnitureHitBox extends SeatOwner {

    Vec3d position();

    Seat<FurnitureHitBox>[] seats();

    AABB[] aabb();

    List<Collider> colliders();

    int[] virtualEntityIds();

    void collectVirtualEntityIds(Consumer<Integer> collector);

    void show(Player player);

    void hide(Player player);

    FurnitureHitBoxConfig<?> config();

    default Optional<EntityHitResult> clip(Vec3d min, Vec3d max) {
        for (AABB value : aabb()) {
            Optional<EntityHitResult> clip = value.clip(min, max);
            if (clip.isPresent()) {
                return clip;
            }
        }
        return Optional.empty();
    }
}
