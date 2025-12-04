package net.momirealms.craftengine.bukkit.entity.furniture;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.List;

public class FurnitureEventListener implements Listener {
    private final BukkitFurnitureManager manager;
    private final BukkitWorldManager worldManager;

    public FurnitureEventListener(final BukkitFurnitureManager manager, final BukkitWorldManager worldManager) {
        this.manager = manager;
        this.worldManager = worldManager;
    }

    /*
     * Load Entities
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntitiesLoadEarly(EntitiesLoadEvent event) {
        List<Entity> entities = event.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof ItemDisplay itemDisplay) {
                this.manager.handleMetaEntityDuringChunkLoad(itemDisplay);
            } else if (BukkitFurnitureManager.COLLISION_ENTITY_CLASS.isInstance(entity)) {
                this.manager.handleCollisionEntityDuringChunkLoad(entity);
            }
        }
        CEWorld world = this.worldManager.getWorld(event.getWorld());
        CEChunk ceChunk = world.getChunkAtIfLoaded(event.getChunk().getChunkKey());
        if (ceChunk != null) {
            ceChunk.setEntitiesLoaded(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onWorldLoad(WorldLoadEvent event) {
        List<Entity> entities = event.getWorld().getEntities();
        for (Entity entity : entities) {
            if (entity instanceof ItemDisplay itemDisplay) {
                this.manager.handleMetaEntityDuringChunkLoad(itemDisplay);
            } else if (BukkitFurnitureManager.COLLISION_ENTITY_CLASS.isInstance(entity)) {
                this.manager.handleCollisionEntityDuringChunkLoad(entity);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityLoad(EntityAddToWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemDisplay itemDisplay) {
            this.manager.handleMetaEntityAfterChunkLoad(itemDisplay);
        } else if (BukkitFurnitureManager.COLLISION_ENTITY_CLASS.isInstance(entity)) {
            this.manager.handleCollisionEntityAfterChunkLoad(entity);
        }
    }

    /*
     * Unload Entities
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Entity[] entities = event.getChunk().getEntities();
        for (Entity entity : entities) {
            if (entity instanceof ItemDisplay itemDisplay) {
                this.manager.handleMetaEntityUnload(itemDisplay);
            } else if (BukkitFurnitureManager.COLLISION_ENTITY_CLASS.isInstance(entity)) {
                this.manager.handleCollisionEntityUnload(entity);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onWorldUnload(WorldUnloadEvent event) {
        List<Entity> entities = event.getWorld().getEntities();
        for (Entity entity : entities) {
            if (entity instanceof ItemDisplay itemDisplay) {
                this.manager.handleMetaEntityUnload(itemDisplay);
            } else if (BukkitFurnitureManager.COLLISION_ENTITY_CLASS.isInstance(entity)) {
                this.manager.handleCollisionEntityUnload(entity);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityUnload(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemDisplay itemDisplay) {
            this.manager.handleMetaEntityUnload(itemDisplay);
        } else if (BukkitFurnitureManager.COLLISION_ENTITY_CLASS.isInstance(entity)) {
            this.manager.handleCollisionEntityUnload(entity);
        }
    }
}
