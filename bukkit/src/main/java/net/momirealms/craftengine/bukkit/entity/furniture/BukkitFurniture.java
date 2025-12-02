package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureConfig;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDataAccessor;
import net.momirealms.craftengine.core.util.QuaternionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class BukkitFurniture extends Furniture {
    private final WeakReference<ItemDisplay> metaEntity;
    private Location location;

    public BukkitFurniture(ItemDisplay metaEntity, FurnitureConfig config, FurnitureDataAccessor data) {
        super(new BukkitEntity(metaEntity), data, config);
        this.metaEntity = new WeakReference<>(metaEntity);
        this.location = metaEntity.getLocation();
    }

    @Override
    public void addCollidersToWorld() {
        Object world = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(this.location.getWorld());
        for (Collider entity : super.colliders) {
            FastNMS.INSTANCE.method$LevelWriter$addFreshEntity(world, entity.handle());
            Entity bukkitEntity = FastNMS.INSTANCE.method$Entity$getBukkitEntity(entity.handle());
            bukkitEntity.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_COLLISION, PersistentDataType.BYTE, (byte) 1);
            bukkitEntity.setPersistent(false);
        }
    }

    @Override
    public void destroy() {
        Optional.ofNullable(this.metaEntity.get()).ifPresent(Entity::remove);
        for (Collider entity : super.colliders) {
            entity.destroy();
        }
    }

    // 获取掉落物的位置，受到家具变种的影响
    public Location getDropLocation() {
        Optional<Vector3f> dropOffset = this.getCurrentVariant().dropOffset();
        if (dropOffset.isEmpty()) {
            return this.location;
        }
        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0, Math.toRadians(180 - this.location.getYaw()), 0).conjugate();
        Vector3f offset = conjugated.transform(new Vector3f(dropOffset.get()));
        return new Location(this.location.getWorld(), this.location.getX() + offset.x, this.location.getY() + offset.y, this.location.getZ() - offset.z);
    }

    public Location location() {
        return location;
    }

    public Entity getBukkitEntity() {
        return this.metaEntity.get();
    }
}