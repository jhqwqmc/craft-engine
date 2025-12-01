package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureConfig;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDataAccessor;
import net.momirealms.craftengine.core.entity.furniture.FurnitureVariant;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.entityculling.CullingData;
import net.momirealms.craftengine.core.util.QuaternionUtils;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

public class BukkitFurniture implements Furniture {
    private static final UUID INVALID_UUID = new UUID(0, 0);
    @NotNull
    private final FurnitureConfig config;
    @NotNull
    private final FurnitureDataAccessor dataAccessor;

    private WeakReference<Entity> baseEntity;
    private FurnitureVariant currentVariant;
    private Location location;
    private boolean valid;
    private UUID uuid;
    private int entityId;

    private FurnitureElement[] elements;

    public BukkitFurniture(@NotNull FurnitureConfig config,
                           @NotNull CompoundTag data) {
        this.dataAccessor = FurnitureDataAccessor.of(data);
        this.currentVariant = Optional.ofNullable(getVariant()).orElseGet(config::anyVariant);
        this.config = config;
        this.baseEntity = new WeakReference<>(null);
        this.valid = false;
        this.entityId = -1;
        this.uuid = INVALID_UUID;
    }

    private void sync() {
        WorldPosition position = position();
        FurnitureElementConfig<?>[] elementConfigs = this.currentVariant.elements();
        FurnitureElement[] elements = new FurnitureElement[elementConfigs.length];
        for (int i = 0; i < elementConfigs.length; i++) {
            FurnitureElement o = elementConfigs[i].create(position);
            elements[i] = o;
        }
        this.elements = elements;
    }

    public void addToWorld(Location location) {
        this.setLocation(location);
        this.valid = true;
        Entity baseEntity = null;  // fixme 处理生成
        this.baseEntity = new WeakReference<>(baseEntity);
        this.uuid = baseEntity.getUniqueId();
        this.entityId = baseEntity.getEntityId();
        this.sync();
    }

    @Override
    public void show(Player player) {
        for (FurnitureElement element : this.elements) {
            element.show(player);
        }
    }

    @Override
    public void hide(Player player) {
        for (FurnitureElement element : this.elements) {
            element.hide(player);
        }
    }

    @Nullable
    @Override
    public CullingData cullingData() {
        return this.config.cullingData();
    }

    @NotNull
    @Override
    public FurnitureConfig config() {
        return this.config;
    }

    @NotNull
    @Override
    public FurnitureDataAccessor dataAccessor() {
        return this.dataAccessor;
    }

    @Override
    public WorldPosition position() {
        return LocationUtils.toWorldPosition(this.location);
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public void destroy() {
        this.valid = false;
        Optional.ofNullable(this.baseEntity.get()).ifPresent(Entity::remove);
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public int entityId() {
        return this.entityId;
    }

    public void teleport(WorldPosition position) {
        Location newLocation = LocationUtils.toLocation(position);
        if (newLocation.equals(this.location)) {
            return;
        }
        this.setLocation(newLocation);
        this.sync();
    }

    public Location location() {
        return location;
    }

    private void setLocation(Location location) {
        this.location = location;
    }

    public FurnitureVariant getVariant() {
        return this.config.getVariant(this.dataAccessor.variant().orElseGet(this.config::anyVariantName));
    }

    public void setVariant(String variant) {
        FurnitureVariant newVariant = this.config.getVariant(variant);
        if (newVariant != this.currentVariant) {
            this.currentVariant = newVariant;
            this.sync();
        }
    }

    // 获取掉落物的位置，受到家具变种的影响
    public Location getDropLocation() {
        Optional<Vector3f> dropOffset = this.getVariant().dropOffset();
        if (dropOffset.isEmpty()) {
            return location();
        }
        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0, Math.toRadians(180 - this.location.getYaw()), 0).conjugate();
        Vector3f offset = conjugated.transform(new Vector3f(dropOffset.get()));
        return new Location(this.location.getWorld(), this.location.getX() + offset.x, this.location.getY() + offset.y, this.location.getZ() - offset.z);
    }
}