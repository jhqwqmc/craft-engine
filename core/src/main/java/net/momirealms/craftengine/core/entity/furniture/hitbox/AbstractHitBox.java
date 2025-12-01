package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.world.EntityHitResult;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.Optional;

public abstract class AbstractHitBox implements HitBox {
    private final Furniture furniture;
    private final HitBoxConfig config;
    private final HitBoxPart[] parts;
    private Seat<HitBox>[] seats;

    public AbstractHitBox(Furniture furniture, HitBoxConfig config, HitBoxPart[] parts) {
        this.parts = parts;
        this.config = config;
        this.furniture = furniture;
    }

    protected abstract void createSeats(HitBoxConfig config);

    @Override
    public HitBoxPart[] parts() {
        return this.parts;
    }

    @Override
    public HitBoxConfig config() {
        return this.config;
    }

    @Override
    public Seat<HitBox>[] seats() {
        return this.seats;
    }

    @Override
    public Optional<EntityHitResult> clip(Vec3d min, Vec3d max) {
        for (HitBoxPart hbe : this.parts) {
            Optional<EntityHitResult> result = hbe.aabb().clip(min, max);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public void saveCustomData(CompoundTag data) {
        data.putString("type", "furniture");
        data.putInt("entity_id", this.furniture.entityId());
    }
}
