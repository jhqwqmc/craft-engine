package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.ColliderType;

public class BukkitCollider implements Collider {
    private final CollisionEntity collisionEntity;

    public BukkitCollider(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding) {
        this.collisionEntity = BukkitFurnitureManager.COLLISION_ENTITY_TYPE == ColliderType.INTERACTION ?
                FastNMS.INSTANCE.createCollisionInteraction(world, aabb, x, y, z, canProjectileHit, canCollide, blocksBuilding) :
                FastNMS.INSTANCE.createCollisionBoat(world, aabb, x, y, z, canProjectileHit, canCollide, blocksBuilding);
    }

    @Override
    public void destroy() {
        this.collisionEntity.destroy();
    }

    @Override
    public int entityId() {
        return this.collisionEntity.getEntityId();
    }

    @Override
    public Object handle() {
        return this.collisionEntity;
    }
}
