package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.entity.furniture.hitbox.InteractionHitBoxConfig;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.entity.furniture.hitbox.HitBoxConfig;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Interaction;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitFurnitureManager extends AbstractFurnitureManager {
    public static final NamespacedKey FURNITURE_KEY = KeyUtils.toNamespacedKey(FurnitureManager.FURNITURE_KEY);
    public static final NamespacedKey FURNITURE_EXTRA_DATA_KEY = KeyUtils.toNamespacedKey(FurnitureManager.FURNITURE_EXTRA_DATA_KEY);
    public static final NamespacedKey FURNITURE_COLLISION = KeyUtils.toNamespacedKey(FurnitureManager.FURNITURE_COLLISION);
    public static Class<?> COLLISION_ENTITY_CLASS = Interaction.class;
    public static Object NMS_COLLISION_ENTITY_TYPE = MEntityTypes.INTERACTION;
    public static ColliderType COLLISION_ENTITY_TYPE = ColliderType.INTERACTION;
    private static BukkitFurnitureManager instance;
    private final BukkitCraftEngine plugin;
    private final Map<Integer, BukkitFurniture> furnitureByRealEntityId = new ConcurrentHashMap<>(256, 0.5f);
    private final Map<Integer, BukkitFurniture> furnitureByEntityId = new ConcurrentHashMap<>(512, 0.5f);
    // Event listeners
    private final FurnitureEventListener furnitureEventListener;

    public static BukkitFurnitureManager instance() {
        return instance;
    }

    public BukkitFurnitureManager(BukkitCraftEngine plugin) {
        super(plugin);
        instance = this;
        this.plugin = plugin;
        this.furnitureEventListener = new FurnitureEventListener(this);
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
        COLLISION_ENTITY_CLASS = Config.colliderType() == ColliderType.INTERACTION ? Interaction.class : Boat.class;
        NMS_COLLISION_ENTITY_TYPE = Config.colliderType() == ColliderType.INTERACTION ? MEntityTypes.INTERACTION : MEntityTypes.OAK_BOAT;
        COLLISION_ENTITY_TYPE = Config.colliderType();
        Bukkit.getPluginManager().registerEvents(this.furnitureEventListener, this.plugin.javaPlugin());
//        if (VersionHelper.isFolia()) {
//            BiConsumer<Entity, Runnable> taskExecutor = (entity, runnable) -> entity.getScheduler().run(this.plugin.javaPlugin(), (t) -> runnable.run(), () -> {});
//            for (World world : Bukkit.getWorlds()) {
//                List<Entity> entities = world.getEntities();
//                for (Entity entity : entities) {
//                    if (entity instanceof ItemDisplay display) {
//                        taskExecutor.accept(entity, () -> handleBaseEntityLoadEarly(display));
//                    } else if (entity instanceof Interaction interaction) {
//                        taskExecutor.accept(entity, () -> handleCollisionEntityLoadOnEntitiesLoad(interaction));
//                    } else if (entity instanceof Boat boat) {
//                        taskExecutor.accept(entity, () -> handleCollisionEntityLoadOnEntitiesLoad(boat));
//                    }
//                }
//            }
//        } else {
//            for (World world : Bukkit.getWorlds()) {
//                List<Entity> entities = world.getEntities();
//                for (Entity entity : entities) {
//                    if (entity instanceof ItemDisplay display) {
//                        handleBaseEntityLoadEarly(display);
//                    } else if (entity instanceof Interaction interaction) {
//                        handleCollisionEntityLoadOnEntitiesLoad(interaction);
//                    } else if (entity instanceof Boat boat) {
//                        handleCollisionEntityLoadOnEntitiesLoad(boat);
//                    }
//                }
//            }
//        }
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this.furnitureEventListener);
        unload();
    }

    @Override
    public boolean isFurnitureRealEntity(int entityId) {
        return this.furnitureByRealEntityId.containsKey(entityId);
    }

    @Nullable
    @Override
    public BukkitFurniture loadedFurnitureByRealEntityId(int entityId) {
        return this.furnitureByRealEntityId.get(entityId);
    }

    @Override
    @Nullable
    public BukkitFurniture loadedFurnitureByEntityId(int entityId) {
        return this.furnitureByEntityId.get(entityId);
    }

//
//    protected void handleBaseEntityUnload(Entity entity) {
//        int id = entity.getEntityId();
//        BukkitFurniture furniture = this.furnitureByRealEntityId.remove(id);
//        if (furniture != null) {
//            Location location = entity.getLocation();
//            boolean isPreventing = FastNMS.INSTANCE.method$ServerLevel$isPreventingStatusUpdates(FastNMS.INSTANCE.field$CraftWorld$ServerLevel(location.getWorld()), location.getBlockX() >> 4, location.getBlockZ() >> 4);
//            if (!isPreventing) {
//                furniture.destroySeats();
//            }
//            for (int sub : furniture.entityIds()) {
//                this.furnitureByEntityId.remove(sub);
//            }
//        }
//    }
//
//    protected void handleCollisionEntityUnload(Entity entity) {
//        int id = entity.getEntityId();
//        this.furnitureByRealEntityId.remove(id);
//    }
//
//    @SuppressWarnings("deprecation") // just a misleading name `getTrackedPlayers`
//    protected void handleBaseEntityLoadLate(ItemDisplay display, int depth) {
//        // must be a furniture item
//        String id = display.getPersistentDataContainer().get(FURNITURE_KEY, PersistentDataType.STRING);
//        if (id == null) return;
//
//        Key key = Key.of(id);
//        Optional<FurnitureConfig> optionalFurniture = furnitureById(key);
//        if (optionalFurniture.isEmpty()) return;
//
//        FurnitureConfig customFurniture = optionalFurniture.get();
//        BukkitFurniture previous = this.furnitureByRealEntityId.get(display.getEntityId());
//        if (previous != null) return;
//
//        Location location = display.getLocation();
//        boolean above1_20_1 = VersionHelper.isOrAbove1_20_2();
//        boolean preventChange = FastNMS.INSTANCE.method$ServerLevel$isPreventingStatusUpdates(FastNMS.INSTANCE.field$CraftWorld$ServerLevel(location.getWorld()), location.getBlockX() >> 4, location.getBlockZ() >> 4);
//        if (above1_20_1) {
//            if (!preventChange) {
//                BukkitFurniture furniture = addNewFurniture(display, customFurniture);
//                furniture.initializeColliders();
//                for (Player player : display.getTrackedPlayers()) {
//                    BukkitAdaptors.adapt(player).entityPacketHandlers().computeIfAbsent(furniture.baseEntityId(), k -> new FurniturePacketHandler(furniture.fakeEntityIds()));
//                    this.plugin.networkManager().sendPacket(BukkitAdaptors.adapt(player), furniture.spawnPacket(player));
//                }
//            }
//        } else {
//            BukkitFurniture furniture = addNewFurniture(display, customFurniture);
//            for (Player player : display.getTrackedPlayers()) {
//                BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(player);
//                serverPlayer.entityPacketHandlers().computeIfAbsent(furniture.baseEntityId(), k -> new FurniturePacketHandler(furniture.fakeEntityIds()));
//                this.plugin.networkManager().sendPacket(serverPlayer, furniture.spawnPacket(player));
//            }
//            if (preventChange) {
//                this.plugin.scheduler().sync().runLater(furniture::initializeColliders, 1, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
//            } else {
//                furniture.initializeColliders();
//            }
//        }
//        if (depth > 2) return;
//        this.plugin.scheduler().sync().runLater(() -> handleBaseEntityLoadLate(display, depth + 1), 1, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
//    }
//
//    protected void handleCollisionEntityLoadLate(Entity entity, int depth) {
//        // remove the entity if it's not a collision entity, it might be wrongly copied by WorldEdit
//        if (FastNMS.INSTANCE.method$CraftEntity$getHandle(entity) instanceof CollisionEntity) {
//            return;
//        }
//        // not a collision entity
//        Byte flag = entity.getPersistentDataContainer().get(FURNITURE_COLLISION, PersistentDataType.BYTE);
//        if (flag == null || flag != 1) {
//            return;
//        }
//
//        Location location = entity.getLocation();
//        World world = location.getWorld();
//        int chunkX = location.getBlockX() >> 4;
//        int chunkZ = location.getBlockZ() >> 4;
//        if (!FastNMS.INSTANCE.method$ServerLevel$isPreventingStatusUpdates(FastNMS.INSTANCE.field$CraftWorld$ServerLevel(world), chunkX, chunkZ)) {
//            entity.remove();
//            return;
//        }
//
//        if (depth > 2) return;
//        plugin.scheduler().sync().runLater(() -> {
//            handleCollisionEntityLoadLate(entity, depth + 1);
//        }, 1, world, chunkX, chunkZ);
//    }
//
//    public void handleBaseEntityLoadEarly(ItemDisplay display) {
//        String id = display.getPersistentDataContainer().get(FURNITURE_KEY, PersistentDataType.STRING);
//        if (id == null) return;
//        // Remove the entity if it's not a valid furniture
//        if (Config.handleInvalidFurniture()) {
//            String mapped = Config.furnitureMappings().get(id);
//            if (mapped != null) {
//                if (mapped.isEmpty()) {
//                    display.remove();
//                    return;
//                } else {
//                    id = mapped;
//                    display.getPersistentDataContainer().set(FURNITURE_KEY, PersistentDataType.STRING, id);
//                }
//            }
//        }
//
//        Key key = Key.of(id);
//        Optional<FurnitureConfig> optionalFurniture = furnitureById(key);
//        if (optionalFurniture.isPresent()) {
//            FurnitureConfig customFurniture = optionalFurniture.get();
//            BukkitFurniture previous = this.furnitureByRealEntityId.get(display.getEntityId());
//            if (previous != null) return;
//            BukkitFurniture furniture = addNewFurniture(display, customFurniture);
//            furniture.initializeColliders(); // safely do it here
//        }
//    }
//
//    public void handleCollisionEntityLoadOnEntitiesLoad(Entity collisionEntity) {
//        // faster
//        if (FastNMS.INSTANCE.method$CraftEntity$getHandle(collisionEntity) instanceof CollisionEntity) {
//            collisionEntity.remove();
//            return;
//        }
//
//        // not a collision entity
//        Byte flag = collisionEntity.getPersistentDataContainer().get(FURNITURE_COLLISION, PersistentDataType.BYTE);
//        if (flag == null || flag != 1) {
//            return;
//        }
//
//        collisionEntity.remove();
//    }
//
//    private FurnitureDataAccessor getFurnitureExtraData(Entity baseEntity) throws IOException {
//        byte[] extraData = baseEntity.getPersistentDataContainer().get(FURNITURE_EXTRA_DATA_KEY, PersistentDataType.BYTE_ARRAY);
//        if (extraData == null) return FurnitureDataAccessor.builder().build();
//        return FurnitureDataAccessor.fromBytes(extraData);
//    }
//
//    private synchronized BukkitFurniture addNewFurniture(ItemDisplay display, FurnitureConfig furniture) {
//        FurnitureDataAccessor extraData;
//        try {
//            extraData = getFurnitureExtraData(display);
//        } catch (IOException e) {
//            extraData = FurnitureDataAccessor.builder().build();
//            plugin.logger().warn("Furniture extra data could not be loaded", e);
//        }
//        BukkitFurniture bukkitFurniture = new BukkitFurniture(display, furniture, extraData);
//        this.furnitureByRealEntityId.put(bukkitFurniture.baseEntityId(), bukkitFurniture);
//        for (int entityId : bukkitFurniture.entityIds()) {
//            this.furnitureByEntityId.put(entityId, bukkitFurniture);
//        }
//        for (Collider collisionEntity : bukkitFurniture.collisionEntities()) {
//            int collisionEntityId = FastNMS.INSTANCE.method$Entity$getId(collisionEntity.handle());
//            this.furnitureByRealEntityId.put(collisionEntityId, bukkitFurniture);
//        }
//        return bukkitFurniture;
//    }

    @Override
    protected HitBoxConfig defaultHitBox() {
        return InteractionHitBoxConfig.DEFAULT;
    }
}
