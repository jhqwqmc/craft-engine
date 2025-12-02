package net.momirealms.craftengine.core.entity.furniture;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import net.momirealms.craftengine.core.world.collision.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.UUID;

public abstract class Furniture implements Cullable {
    public final FurnitureConfig config;
    public final FurnitureDataAccessor dataAccessor;
    public final WeakReference<Entity> metaDataEntity;

    protected CullingData cullingData;
    protected FurnitureVariant currentVariant;
    protected FurnitureElement[] elements;
    protected Collider[] colliders;
    protected FurnitureHitBox[] hitboxes;
    protected Int2ObjectMap<FurnitureHitBox> hitboxMap;
    protected int[] virtualEntityIds;
    protected int[] colliderEntityIds;

    protected Furniture(Entity metaDataEntity, FurnitureDataAccessor data, FurnitureConfig config) {
        this.config = config;
        this.dataAccessor = data;
        this.metaDataEntity = new WeakReference<>(metaDataEntity);
        this.setVariant(config.getVariant(data));
    }

    public WeakReference<Entity> metaDataEntity() {
        return this.metaDataEntity;
    }

    public FurnitureVariant getCurrentVariant() {
        return this.currentVariant;
    }

    public void setVariant(FurnitureVariant variant) {
        this.currentVariant = variant;
        this.hitboxMap = new Int2ObjectOpenHashMap<>();
        // 初始化家具元素
        IntList virtualEntityIds = new IntArrayList();
        FurnitureElementConfig<?>[] elementConfigs = variant.elementConfigs();
        this.elements = new FurnitureElement[elementConfigs.length];
        for (int i = 0; i < elementConfigs.length; i++) {
            FurnitureElement element = elementConfigs[i].create(this);
            this.elements[i] = element;
            element.collectVirtualEntityId(virtualEntityIds::addLast);
        }
        // 初始化碰撞箱
        FurnitureHitBoxConfig<?>[] furnitureHitBoxConfigs = variant.hitBoxConfigs();
        ObjectArrayList<Collider> colliders = new ObjectArrayList<>(furnitureHitBoxConfigs.length);
        this.hitboxes = new FurnitureHitBox[furnitureHitBoxConfigs.length];
        for (int i = 0; i < furnitureHitBoxConfigs.length; i++) {
            FurnitureHitBox hitbox = furnitureHitBoxConfigs[i].create(this);
            this.hitboxes[i] = hitbox;
            for (int hitboxEntityId : hitbox.virtualEntityIds()) {
                this.hitboxMap.put(hitboxEntityId, hitbox);
            }
            colliders.addAll(hitbox.colliders());
            hitbox.collectVirtualEntityIds(virtualEntityIds::addLast);
        }
        // 虚拟碰撞箱的实体id
        this.virtualEntityIds = virtualEntityIds.toIntArray();
        this.colliders = colliders.toArray(new Collider[0]);
        this.colliderEntityIds = colliders.stream().mapToInt(Collider::entityId).toArray();
        this.cullingData = createCullingData(variant.cullingData());
    }

    private CullingData createCullingData(CullingData parent) {
        if (parent == null) return null;
        AABB aabb = parent.aabb;
        WorldPosition position = position();
        Vec3d pos1 = getRelativePosition(position, new Vector3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ));
        Vec3d pos2 = getRelativePosition(position, new Vector3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ));
        return new CullingData(new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z), parent.maxDistance, parent.aabbExpansion, parent.rayTracing);
    }

    @Nullable
    public FurnitureHitBox hitboxByEntityId(int entityId) {
        return this.hitboxMap.get(entityId);
    }

    @Nullable
    @Override
    public CullingData cullingData() {
        return this.cullingData;
    }

    public Key id() {
        return this.config.id();
    }

    // 会发给玩家的包
    public int[] virtualEntityIds() {
        return this.virtualEntityIds;
    }

    public int[] colliderEntityIds() {
        return colliderEntityIds;
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
        return this.config;
    }

    public FurnitureDataAccessor dataAccessor() {
        return this.dataAccessor;
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
