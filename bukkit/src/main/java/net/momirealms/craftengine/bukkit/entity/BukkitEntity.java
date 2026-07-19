package net.momirealms.craftengine.bukkit.entity;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.entity.data.EntityData;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.entity.CraftEntityProxy;
import net.momirealms.craftengine.proxy.minecraft.core.RegistryProxy;
import net.momirealms.craftengine.proxy.minecraft.core.registries.BuiltInRegistriesProxy;
import net.momirealms.craftengine.proxy.minecraft.network.syncher.SynchedEntityDataProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;

public class BukkitEntity implements net.momirealms.craftengine.core.entity.Entity {
    protected final WeakReference<Object> entity;

    public BukkitEntity(Object entity) {
        if (EntityProxy.CLASS.isInstance(entity)) {
            this.entity = new WeakReference<>(entity);
        } else if (entity instanceof Entity bukkitEntity) {
            this.entity = new WeakReference<>(CraftEntityProxy.INSTANCE.getEntity(bukkitEntity));
        } else {
            throw new IllegalArgumentException(entity.getClass() + " is not a valid Entity");
        }
    }

    protected BukkitEntity(WeakReference<Object> entity) {
        this.entity = entity;
    }

    @Override
    public double x() {
        return EntityProxy.INSTANCE.getX(minecraftEntity());
    }

    @Override
    public double y() {
        return EntityProxy.INSTANCE.getY(minecraftEntity());
    }

    @Override
    public double z() {
        return EntityProxy.INSTANCE.getZ(minecraftEntity());
    }

    @Override
    public WorldPosition position() {
        return LocationUtils.toWorldPosition(platformEntity().getLocation());
    }

    @Override
    public int entityId() {
        return EntityProxy.INSTANCE.getId(minecraftEntity());
    }

    @Override
    public float xRot() {
        return EntityProxy.INSTANCE.getXRot(minecraftEntity());
    }

    @Override
    public float yRot() {
        return EntityProxy.INSTANCE.getYRot(minecraftEntity());
    }

    @Override
    public World world() {
        return BukkitAdaptor.adapt(LevelProxy.INSTANCE.getWorld(EntityProxy.INSTANCE.getLevel(this.minecraftEntity())));
    }

    @Override
    public Direction getDirection() {
        return DirectionUtils.toDirection(platformEntity().getFacing());
    }

    @Override
    public org.bukkit.entity.Entity platformEntity() {
        return EntityProxy.INSTANCE.getBukkitEntity(this.entity.get());
    }

    @Override
    public Object minecraftEntity() {
        return this.entity.get();
    }

    @Override
    public Key type() {
        Object entityType = EntityProxy.INSTANCE.getType(minecraftEntity());
        Object id = RegistryProxy.INSTANCE.getKey(BuiltInRegistriesProxy.ENTITY_TYPE, entityType);
        return KeyUtils.identifierToKey(id);
    }

    @Override
    public boolean isValid() {
        Entity bkEntity = platformEntity();
        if (bkEntity == null) return false;
        return bkEntity.isValid();
    }

    @Override
    public String name() {
        return platformEntity().getName();
    }

    @Override
    public UUID uuid() {
        return EntityProxy.INSTANCE.getUUID(this.minecraftEntity());
    }

    @Override
    public Object entityData() {
        return EntityProxy.INSTANCE.getEntityData(minecraftEntity());
    }

    @Override
    public <T> T getEntityData(EntityData<T> data) {
        return SynchedEntityDataProxy.INSTANCE.get(entityData(), data.entityDataAccessor());
    }

    @Override
    public <T> void setEntityData(EntityData<T> data, T value, boolean force) {
        SynchedEntityDataProxy.INSTANCE.set(entityData(), data.entityDataAccessor(), value, force);
    }

    @Override
    public void remove() {
        this.platformEntity().remove();
    }

    @Override
    public Set<Player> getTrackedBy() {
        return EntityUtils.getTrackedBy(this.platformEntity(), BukkitAdaptor::adapt);
    }

    @Override
    public void teleport(WorldPosition worldPosition) {
        Location location = new Location((org.bukkit.World) worldPosition.world().platformWorld(), worldPosition.x(), worldPosition.y(), worldPosition.z(), worldPosition.yRot(), worldPosition.xRot());
        if (VersionHelper.hasFoliaPatch) {
            this.platformEntity().teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            this.platformEntity().teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    @Override
    public WorldPosition eyePosition() {
        return LocationUtils.toWorldPosition(this.getEyeLocation());
    }

    public Vec3d getEyePos() {
        Object entity = minecraftEntity();
        Object vehicle = EntityProxy.INSTANCE.getVehicle(entity);
        if (vehicle != null) {
            Vec3d mountPos = EntityUtils.getPassengerRidingPosition(vehicle, entity);
            return new Vec3d(mountPos.x, mountPos.y + EntityProxy.INSTANCE.getEyeHeight(entity), mountPos.z);
        } else {
            return new Vec3d(EntityProxy.INSTANCE.getXo(entity), EntityProxy.INSTANCE.getEyeY(entity), EntityProxy.INSTANCE.getZo(entity));
        }
    }

    public Location getEyeLocation() {
        Object entity = minecraftEntity();
        Object vehicle = EntityProxy.INSTANCE.getVehicle(entity);
        if (vehicle != null) {
            Vec3d mountPos = EntityUtils.getPassengerRidingPosition(vehicle, entity);
            return new Location(platformEntity().getWorld(), mountPos.x, mountPos.y + EntityProxy.INSTANCE.getEyeHeight(entity), mountPos.z, EntityProxy.INSTANCE.getYRot(entity), EntityProxy.INSTANCE.getXRot(entity));
        }
        return new Location(platformEntity().getWorld(), EntityProxy.INSTANCE.getXo(this.entity), EntityProxy.INSTANCE.getEyeY(this.entity), EntityProxy.INSTANCE.getZo(this.entity), EntityProxy.INSTANCE.getYRot(entity), EntityProxy.INSTANCE.getXRot(entity));
    }
}
