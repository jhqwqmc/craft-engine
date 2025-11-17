package net.momirealms.craftengine.bukkit.entity.furniture;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.QuaternionUtils;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

public class BukkitFurniture implements Furniture {
    private final CustomFurniture furniture;
    private final CustomFurniture.Placement placement;
    private FurnitureExtraData extraData;
    // location
    private final Location location;
    // base entity
    private final WeakReference<Entity> baseEntity;
    private final int baseEntityId;
    // colliders
    private final Collider[] colliderEntities;
    // cache
    private final List<Integer> fakeEntityIds;
    private final List<Integer> entityIds;
    private final Map<Integer, BukkitHitBox> hitBoxes  = new Int2ObjectArrayMap<>();
    private final Map<Integer, HitBoxPart> hitBoxParts = new Int2ObjectArrayMap<>();
    private final boolean minimized;
    private final boolean hasExternalModel;
    // cached spawn packet
    private Object cachedSpawnPacket;
    private Object cachedMinimizedSpawnPacket;

    public BukkitFurniture(Entity baseEntity,
                           CustomFurniture furniture,
                           FurnitureExtraData extraData) {
        this.extraData = extraData;
        this.baseEntityId = baseEntity.getEntityId();
        this.location = baseEntity.getLocation();
        this.baseEntity = new WeakReference<>(baseEntity);
        this.furniture = furniture;
        this.minimized = furniture.settings().minimized();
        this.placement = furniture.getValidPlacement(extraData.anchorType().orElseGet(furniture::getAnyAnchorType));

        List<Integer> fakeEntityIds = new IntArrayList();
        List<Integer> mainEntityIds = new IntArrayList();
        mainEntityIds.add(this.baseEntityId);

        // 绑定外部模型
        Optional<ExternalModel> optionalExternal = placement.externalModel();
        if (optionalExternal.isPresent()) {
            try {
                optionalExternal.get().bindModel(new BukkitEntity(baseEntity));
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Failed to load external model for furniture " + id(), e);
            }
            this.hasExternalModel = true;
        } else {
            this.hasExternalModel = false;
        }

        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0, Math.toRadians(180 - this.location.getYaw()), 0).conjugate();
        List<Object> packets = new ArrayList<>();
        List<Object> minimizedPackets = new ArrayList<>();
        List<Collider> colliders = new ArrayList<>(4);
        WorldPosition position = position();


        // 初始化家具的元素
        for (FurnitureElement element : placement.elements()) {
            int entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
            fakeEntityIds.add(entityId);
            element.initPackets(this, entityId, conjugated, packet -> {
                packets.add(packet);
                if (this.minimized) minimizedPackets.add(packet);
            });
        }

        // 初始化碰撞箱
        for (HitBoxConfig hitBoxConfig : this.placement.hitBoxConfigs()) {
            int[] ids = hitBoxConfig.acquireEntityIds(CoreReflections.instance$Entity$ENTITY_COUNTER::incrementAndGet);
            List<HitBoxPart> aabbs = new ArrayList<>();

            hitBoxConfig.initPacketsAndColliders(ids, position, conjugated, (packet, canBeMinimized) -> {
                packets.add(packet);
                if (this.minimized && !canBeMinimized) {
                    minimizedPackets.add(packet);
                }
            }, colliders::add, part -> {
                this.hitBoxParts.put(part.entityId(), part);
                aabbs.add(part);
            });

            BukkitHitBox hitBox = new BukkitHitBox(this, hitBoxConfig, aabbs.toArray(new HitBoxPart[0]));
            for (int entityId : ids) {
                fakeEntityIds.add(entityId);
                mainEntityIds.add(entityId);
                this.hitBoxes.put(entityId, hitBox);
            }
        }

        // 初始化缓存的家具包
        try {
            this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(packets);
            if (this.minimized) {
                this.cachedMinimizedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(minimizedPackets);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to init spawn packets for furniture " + id(), e);
        }


        this.fakeEntityIds = fakeEntityIds;
        this.entityIds = mainEntityIds;
        this.colliderEntities = colliders.toArray(new Collider[0]);
    }

    @Override
    public void initializeColliders() {
        Object world = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(this.location.getWorld());
        for (Collider entity : this.colliderEntities) {
            FastNMS.INSTANCE.method$LevelWriter$addFreshEntity(world, entity.handle());
            Entity bukkitEntity = FastNMS.INSTANCE.method$Entity$getBukkitEntity(entity.handle());
            bukkitEntity.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_COLLISION, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @NotNull
    public Object spawnPacket(Player player) {
        // TODO hasPermission might be slow, can we use a faster way in the future?
        // TODO Make it based on conditions. So we can dynamically control which furniture should be sent to the player
        if (!this.minimized || player.hasPermission(FurnitureManager.FURNITURE_ADMIN_NODE)) {
            return this.cachedSpawnPacket;
        } else {
            return this.cachedMinimizedSpawnPacket;
        }
    }

    @Override
    public WorldPosition position() {
        return LocationUtils.toWorldPosition(this.location);
    }

    @NotNull
    public Location location() {
        return this.location.clone();
    }

    @NotNull
    public Entity baseEntity() {
        Entity entity = this.baseEntity.get();
        if (entity == null) {
            throw new RuntimeException("Base entity not found. It might be unloaded.");
        }
        return entity;
    }

    @Override
    public boolean isValid() {
        return baseEntity().isValid();
    }

    @NotNull
    public Location dropLocation() {
        Optional<Vector3f> dropOffset = this.placement.dropOffset();
        if (dropOffset.isEmpty()) {
            return location();
        }
        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0, Math.toRadians(180 - this.location.getYaw()), 0).conjugate();
        Vector3f offset = conjugated.transform(new Vector3f(dropOffset.get()));
        return new Location(this.location.getWorld(), this.location.getX() + offset.x, this.location.getY() + offset.y, this.location.getZ() - offset.z);
    }

    @Override
    public void destroy() {
        if (!isValid()) {
            return;
        }
        this.baseEntity().remove();
        this.destroyColliders();
        this.destroySeats();
    }

    @Override
    public void destroyColliders() {
        for (Collider entity : this.colliderEntities) {
            if (entity != null)
                entity.destroy();
        }
    }

    @Override
    public void destroySeats() {
        for (HitBox hitBox : this.hitBoxes.values()) {
            for (Seat<HitBox> seat : hitBox.seats()) {
                seat.destroy();
            }
        }
    }

    @Override
    public UUID uuid() {
        return this.baseEntity().getUniqueId();
    }

    @Override
    public int baseEntityId() {
        return this.baseEntityId;
    }

    @NotNull
    public List<Integer> entityIds() {
        return Collections.unmodifiableList(this.entityIds);
    }

    @NotNull
    public List<Integer> fakeEntityIds() {
        return Collections.unmodifiableList(this.fakeEntityIds);
    }

    public Collider[] collisionEntities() {
        return this.colliderEntities;
    }

    @Override
    public @Nullable HitBox hitBoxByEntityId(int id) {
        return this.hitBoxes.get(id);
    }

    @Override
    public @Nullable HitBoxPart hitBoxPartByEntityId(int id) {
        return this.hitBoxParts.get(id);
    }

    @Override
    public @NotNull AnchorType anchorType() {
        return this.placement.anchorType();
    }

    @Override
    public @NotNull Key id() {
        return this.furniture.id();
    }

    @Override
    public @NotNull CustomFurniture config() {
        return this.furniture;
    }

    @Override
    public boolean hasExternalModel() {
        return hasExternalModel;
    }

    @Override
    public FurnitureExtraData extraData() {
        return this.extraData;
    }

    @Override
    public void setExtraData(FurnitureExtraData extraData) {
        this.extraData = extraData;
        this.save();
    }

    @Override
    public void save() {
        try {
            this.baseEntity().getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_EXTRA_DATA_KEY, PersistentDataType.BYTE_ARRAY, this.extraData.toBytes());
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Failed to save furniture data.", e);
        }
    }
}