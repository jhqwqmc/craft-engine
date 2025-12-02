package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.entity.furniture.hitbox.InteractionFurnitureHitboxConfig;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class BukkitFurnitureManager extends AbstractFurnitureManager {
    public static final NamespacedKey FURNITURE_KEY = KeyUtils.toNamespacedKey(FurnitureManager.FURNITURE_KEY);
    public static final NamespacedKey FURNITURE_EXTRA_DATA_KEY = KeyUtils.toNamespacedKey(FurnitureManager.FURNITURE_EXTRA_DATA_KEY);
    public static final NamespacedKey FURNITURE_COLLISION = KeyUtils.toNamespacedKey(FurnitureManager.FURNITURE_COLLISION);
    private static BukkitFurnitureManager instance;

    public static Class<?> COLLISION_ENTITY_CLASS = Interaction.class;
    public static Object NMS_COLLISION_ENTITY_TYPE = MEntityTypes.INTERACTION;
    public static ColliderType COLLISION_ENTITY_TYPE = ColliderType.INTERACTION;

    private final BukkitCraftEngine plugin;

    private final Map<Integer, BukkitFurniture> byMetaEntityId = new ConcurrentHashMap<>(256, 0.5f);
    private final Map<Integer, BukkitFurniture> byEntityId = new ConcurrentHashMap<>(512, 0.5f);
    // Event listeners
    private final FurnitureEventListener furnitureEventListener;

    public static BukkitFurnitureManager instance() {
        return instance;
    }

    public BukkitFurnitureManager(BukkitCraftEngine plugin) {
        super(plugin);
        instance = this;
        this.plugin = plugin;
        this.furnitureEventListener = new FurnitureEventListener(this, plugin.worldManager());
    }

    @Override
    public Furniture place(WorldPosition position, FurnitureConfig furniture, FurnitureDataAccessor dataAccessor, boolean playSound) {
//        return this.place(LocationUtils.toLocation(position), furniture, dataAccessor, playSound);
        return null;
    }

    public BukkitFurniture place(Location location, FurnitureConfig furniture, FurnitureDataAccessor extraData, boolean playSound) {
//        Optional<AnchorType> optionalAnchorType = extraData.anchorType();
//        if (optionalAnchorType.isEmpty() || !furniture.isAllowedPlacement(optionalAnchorType.get())) {
//            extraData.anchorType(furniture.getAnyAnchorType());
//        }
//        Entity furnitureEntity = EntityUtils.spawnEntity(location.getWorld(), location, EntityType.ITEM_DISPLAY, entity -> {
//            ItemDisplay display = (ItemDisplay) entity;
//            display.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_KEY, PersistentDataType.STRING, furniture.id().toString());
//            try {
//                display.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_EXTRA_DATA_KEY, PersistentDataType.BYTE_ARRAY, extraData.toBytes());
//            } catch (IOException e) {
//                this.plugin.logger().warn("Failed to set furniture PDC for " + furniture.id().toString(), e);
//            }
//            handleBaseEntityLoadEarly(display);
//        });
//        if (playSound) {
//            SoundData data = furniture.settings().sounds().placeSound();
//            location.getWorld().playSound(location, data.id().toString(), SoundCategory.BLOCKS, data.volume().get(), data.pitch().get());
//        }
//        return loadedFurnitureByRealEntityId(furnitureEntity.getEntityId());
        return null;
    }

    @Override
    public void delayedInit() {
        // 确定碰撞箱实体类型
        COLLISION_ENTITY_TYPE = Config.colliderType();
        COLLISION_ENTITY_CLASS = Config.colliderType() == ColliderType.INTERACTION ? Interaction.class : Boat.class;
        NMS_COLLISION_ENTITY_TYPE = Config.colliderType() == ColliderType.INTERACTION ? MEntityTypes.INTERACTION : MEntityTypes.OAK_BOAT;

        // 注册事件
        Bukkit.getPluginManager().registerEvents(this.furnitureEventListener, this.plugin.javaPlugin());

        // 对世界上已有实体的记录
        if (VersionHelper.isFolia()) {
            BiConsumer<Entity, Runnable> taskExecutor = (entity, runnable) -> entity.getScheduler().run(this.plugin.javaPlugin(), (t) -> runnable.run(), () -> {});
            for (World world : Bukkit.getWorlds()) {
                List<Entity> entities = world.getEntities();
                for (Entity entity : entities) {
                    if (entity instanceof ItemDisplay display) {
                        taskExecutor.accept(entity, () -> handleMetaEntityDuringChunkLoad(display));
                    } else if (entity instanceof Interaction interaction) {
                        taskExecutor.accept(entity, () -> handleCollisionEntityDuringChunkLoad(interaction));
                    } else if (entity instanceof Boat boat) {
                        taskExecutor.accept(entity, () -> handleCollisionEntityDuringChunkLoad(boat));
                    }
                }
            }
        } else {
            for (World world : Bukkit.getWorlds()) {
                List<Entity> entities = world.getEntities();
                for (Entity entity : entities) {
                    if (entity instanceof ItemDisplay display) {
                        handleMetaEntityDuringChunkLoad(display);
                    } else if (entity instanceof Interaction interaction) {
                        handleCollisionEntityDuringChunkLoad(interaction);
                    } else if (entity instanceof Boat boat) {
                        handleCollisionEntityDuringChunkLoad(boat);
                    }
                }
            }
        }
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this.furnitureEventListener);
        unload();
    }

    @Override
    public boolean isFurnitureRealEntity(int entityId) {
        return this.byMetaEntityId.containsKey(entityId);
    }

    @Nullable
    @Override
    public BukkitFurniture loadedFurnitureByRealEntityId(int entityId) {
        return this.byMetaEntityId.get(entityId);
    }

    @Override
    @Nullable
    public BukkitFurniture loadedFurnitureByEntityId(int entityId) {
        return this.byEntityId.get(entityId);
    }

    protected void handleMetaEntityUnload(Entity entity) {
        int id = entity.getEntityId();
        BukkitFurniture furniture = this.byMetaEntityId.remove(id);
        if (furniture != null) {
            Location location = entity.getLocation();
            // 区块还在加载的时候，就重复卸载了
            boolean isPreventing = FastNMS.INSTANCE.method$ServerLevel$isPreventingStatusUpdates(FastNMS.INSTANCE.field$CraftWorld$ServerLevel(location.getWorld()), location.getBlockX() >> 4, location.getBlockZ() >> 4);
            if (!isPreventing) {
                furniture.destroySeats();
            }
            for (int sub : furniture.entityIds()) {
                this.byEntityId.remove(sub);
            }
        }
    }

    protected void handleCollisionEntityUnload(Entity entity) {
        int id = entity.getEntityId();
        this.byMetaEntityId.remove(id);
    }

    private boolean isEntitiesLoaded(Location location) {
        CEWorld ceWorld = this.plugin.worldManager().getWorld(location.getWorld());
        CEChunk ceChunk = ceWorld.getChunkAtIfLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        if (ceChunk == null) return false;
        return ceChunk.isEntitiesLoaded();
    }

    protected void handleMetaEntityDuringChunkLoad(ItemDisplay entity) {
        // 实体可能不是持久的
        if (!entity.isPersistent()) {
            return;
        }

        // 获取家具pdc
        String id = entity.getPersistentDataContainer().get(FURNITURE_KEY, PersistentDataType.STRING);
        if (id == null) return;

        // 处理无效的家具
        if (Config.handleInvalidFurniture()) {
            String mapped = Config.furnitureMappings().get(id);
            if (mapped != null) {
                if (mapped.isEmpty()) {
                    entity.remove();
                    return;
                } else {
                    id = mapped;
                    entity.getPersistentDataContainer().set(FURNITURE_KEY, PersistentDataType.STRING, id);
                }
            }
        }

        // 获取家具配置
        Key key = Key.of(id);
        Optional<FurnitureConfig> optionalFurniture = furnitureById(key);
        if (optionalFurniture.isEmpty()) return;

        // 已经在其他事件里加载过了
        FurnitureConfig customFurniture = optionalFurniture.get();
        BukkitFurniture previous = this.byMetaEntityId.get(entity.getEntityId());
        if (previous != null) return;

        BukkitFurniture furniture = addNewFurniture(entity, customFurniture);
        furniture.addCollidersToWorld();
    }

    protected void handleMetaEntityAfterChunkLoad(ItemDisplay entity) {
        // 实体可能不是持久的
        if (!entity.isPersistent()) {
            return;
        }

        // 获取家具pdc
        String id = entity.getPersistentDataContainer().get(FURNITURE_KEY, PersistentDataType.STRING);
        if (id == null) return;

        // 这个区块还处于加载实体中，这个时候不处理
        if (!isEntitiesLoaded(entity.getLocation())) {
            return;
        }

        // 获取家具配置
        Key key = Key.of(id);
        Optional<FurnitureConfig> optionalFurniture = furnitureById(key);
        if (optionalFurniture.isEmpty()) return;

        // 已经在其他事件里加载过了
        FurnitureConfig customFurniture = optionalFurniture.get();
        BukkitFurniture previous = this.byMetaEntityId.get(entity.getEntityId());
        if (previous != null) return;

        BukkitFurniture furniture = addNewFurniture(entity, customFurniture);
        furniture.addCollidersToWorld();
    }

    protected void handleCollisionEntityAfterChunkLoad(Entity entity) {
        // 实体可能不是持久的
        if (!entity.isPersistent()) {
            return;
        }
        // 如果是碰撞实体，那么就忽略
        if (FastNMS.INSTANCE.method$CraftEntity$getHandle(entity) instanceof CollisionEntity) {
            return;
        }
        // 看看有没有碰撞实体的pdc
        Byte flag = entity.getPersistentDataContainer().get(FURNITURE_COLLISION, PersistentDataType.BYTE);
        if (flag == null || flag != 1) {
            return;
        }
        // 实体未加载
        if (!isEntitiesLoaded(entity.getLocation())) {
            return;
        }
        // 移除被WorldEdit错误复制的碰撞实体
        entity.remove();
    }

    public void handleCollisionEntityDuringChunkLoad(Entity collisionEntity) {
        // faster
        if (FastNMS.INSTANCE.method$CraftEntity$getHandle(collisionEntity) instanceof CollisionEntity) {
            collisionEntity.remove();
            return;
        }

        // not a collision entity
        Byte flag = collisionEntity.getPersistentDataContainer().get(FURNITURE_COLLISION, PersistentDataType.BYTE);
        if (flag == null || flag != 1) {
            return;
        }

        collisionEntity.remove();
    }

    private FurnitureDataAccessor getFurnitureDataAccessor(Entity baseEntity) {
        byte[] extraData = baseEntity.getPersistentDataContainer().get(FURNITURE_EXTRA_DATA_KEY, PersistentDataType.BYTE_ARRAY);
        if (extraData == null) return new FurnitureDataAccessor(null);
        try {
            return FurnitureDataAccessor.fromBytes(extraData);
        } catch (IOException e) {
            // 损坏了？一般不会
            return new FurnitureDataAccessor(null);
        }
    }

    private synchronized BukkitFurniture addNewFurniture(ItemDisplay display, FurnitureConfig furniture) {
        BukkitFurniture bukkitFurniture = new BukkitFurniture(display, furniture, getFurnitureDataAccessor(display));
        this.byMetaEntityId.put(display.getEntityId(), bukkitFurniture);
        for (int entityId : bukkitFurniture.entityIds()) {
            this.byEntityId.put(entityId, bukkitFurniture);
        }
        for (Collider collisionEntity : bukkitFurniture.colliders()) {
            int collisionEntityId = FastNMS.INSTANCE.method$Entity$getId(collisionEntity.handle());
            this.byEntityId.put(collisionEntityId, bukkitFurniture);
        }
        return bukkitFurniture;
    }

    @Override
    protected FurnitureHitBoxConfig<?> defaultHitBox() {
        return InteractionFurnitureHitboxConfig.DEFAULT;
    }
}
