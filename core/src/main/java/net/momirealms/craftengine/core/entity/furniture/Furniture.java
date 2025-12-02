package net.momirealms.craftengine.core.entity.furniture;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.plugin.entityculling.CullingData;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.QuaternionUtils;
import net.momirealms.craftengine.core.world.Cullable;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Furniture implements Cullable {
    public final FurnitureConfig config;
    public final FurnitureDataAccessor dataAccessor;
    public final WeakReference<Entity> metaDataEntity;

    protected FurnitureVariant currentVariant;
    protected FurnitureElement[] elements;
    protected Collider[] colliders;
    protected FurnitureHitBox[] hitboxes;

    protected Int2ObjectMap<FurnitureHitBox> hitboxMap;
    protected int[] entityIds;

    protected Furniture(Entity metaDataEntity, FurnitureDataAccessor data, FurnitureConfig config) {
        this.config = config;
        this.dataAccessor = data;
        this.metaDataEntity = new WeakReference<>(metaDataEntity);
    }

    public WeakReference<Entity> metaDataEntity() {
        return this.metaDataEntity;
    }

    public FurnitureVariant getCurrentVariant() {
        return currentVariant;
    }

    public String getCurrentVariantName() {
        return null;
    }

    public void setVariant(FurnitureVariant variant) {
        this.currentVariant = variant;
        WorldPosition position = this.position();
        // 初始化家具元素
        FurnitureElementConfig<?>[] elementConfigs = variant.elementConfigs();
        this.elements = new FurnitureElement[elementConfigs.length];
        for (int i = 0; i < elementConfigs.length; i++) {
            this.elements[i] = elementConfigs[i].create(this);
        }
        // 初始化碰撞箱
        FurnitureHitBoxConfig<?>[] furnitureHitBoxConfigs = variant.furnitureHitBoxConfigs();
        List<Collider> colliders = new ArrayList<>(furnitureHitBoxConfigs.length);
        this.hitboxes = new FurnitureHitBox[furnitureHitBoxConfigs.length];
        for (int i = 0; i < furnitureHitBoxConfigs.length; i++) {
            FurnitureHitBox hitbox = furnitureHitBoxConfigs[i].create(this);
            this.hitboxes[i] = hitbox;
            for (int hitboxEntityId : hitbox.virtualEntityIds()) {
                this.hitboxMap.put(hitboxEntityId, hitbox);
            }
            Collider collider = hitbox.collider();
            if (collider != null) {
                colliders.add(collider);
            }
        }
        this.colliders = colliders.toArray(new Collider[0]);
    }

    @Nullable
    public FurnitureHitBox hitboxByEntityId(int entityId) {
        return this.hitboxMap.get(entityId);
    }

    @Nullable
    @Override
    public CullingData cullingData() {
        return this.config.cullingData();
    }

    public Key id() {
        return this.config.id();
    }

    public int[] entityIds() {
        return this.entityIds;
    }

    public UUID uuid() {
        Entity entity = this.metaDataEntity.get();
        if (entity == null) return null;
        return entity.uuid();
    }

    @Override
    public void show(Player player) {
        for (FurnitureElement element : this.elements) {
            element.show(player);
        }
        for (FurnitureHitBox hitbox : this.hitboxes) {
            hitbox.show(player);
        }
    }

    @Override
    public void hide(Player player) {
        for (FurnitureElement element : this.elements) {
            element.hide(player);
        }
        for (FurnitureHitBox hitbox : this.hitboxes) {
            hitbox.hide(player);
        }
    }

    public abstract void addCollidersToWorld();

    public void destroySeats() {
        for (FurnitureHitBox hitbox : this.hitboxes) {
            for (Seat<FurnitureHitBox> seat : hitbox.seats()) {
                seat.destroy();
            }
        }
    }

    public boolean isValid() {
        Entity entity = this.metaDataEntity.get();
        if (entity == null) return false;
        return entity.isValid();
    }

    public abstract void destroy();

    public FurnitureConfig config() {
        return config;
    }

    public FurnitureDataAccessor dataAccessor() {
        return dataAccessor;
    }

    public Collider[] colliders() {
        return this.colliders;
    }

    public WorldPosition position() {
        Entity entity = this.metaDataEntity.get();
        if (entity == null) return null;
        return entity.position();
    }

    public int entityId() {
        Entity entity = this.metaDataEntity.get();
        if (entity == null) return -1;
        return entity.entityID();
    }

    public Vec3d getRelativePosition(Vector3f position) {
        return getRelativePosition(this.position(), position);
    }

    public static Vec3d getRelativePosition(WorldPosition location, Vector3f position) {
        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0f, (float) Math.toRadians(180 - location.yRot()), 0f).conjugate();
        Vector3f offset = conjugated.transform(new Vector3f(position));
        return new Vec3d(location.x + offset.x, location.y + offset.y, location.z - offset.z);
    }

    public World world() {
        Entity entity = this.metaDataEntity.get();
        if (entity == null) return null;
        return entity.world();
    }
}
