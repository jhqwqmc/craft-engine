package net.momirealms.craftengine.bukkit.entity.furniture;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.api.event.FurnitureInteractEvent;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractFurniture(FurnitureInteractEvent event) {
        Player bukkitPlayer = event.getPlayer();
        BukkitServerPlayer player = BukkitAdaptors.adapt(bukkitPlayer);
        if (!(player.canInstabuild() && player.hasPermission("minecraft.debugstick")) && !player.hasPermission("minecraft.debugstick.always")) {
            return;
        }
        Item<ItemStack> itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!itemInHand.vanillaId().equals(ItemKeys.DEBUG_STICK)) return;
        BukkitFurniture furniture = event.furniture();
        List<String> variants = new ArrayList<>(furniture.config.variants().keySet());
        if (variants.size() == 1) {
            try {
                Object systemChatPacket = NetworkReflections.constructor$ClientboundSystemChatPacket.newInstance(
                        ComponentUtils.adventureToMinecraft(Component.translatable("item.minecraft.debug_stick.empty").arguments(Component.text(furniture.id().asString()))), true);
                player.sendPacket(systemChatPacket, false);
            } catch (ReflectiveOperationException e) {
                CraftEngine.instance().logger().warn("Could not create system chat packet", e);
            }
        } else {
            String variantName = furniture.getCurrentVariant().name();
            int index = variants.indexOf(variantName) + 1;
            if (index >= variants.size()) {
                index = 0;
            }
            furniture.setVariant(variants.get(index));
            try {
                Object systemChatPacket = NetworkReflections.constructor$ClientboundSystemChatPacket.newInstance(
                        ComponentUtils.adventureToMinecraft(Component.translatable("item.minecraft.debug_stick.update")
                                .arguments(
                                        Component.text("variant"),
                                        Component.text(variants.get(index))
                                )), true);
                player.sendPacket(systemChatPacket, false);
            } catch (ReflectiveOperationException e) {
                CraftEngine.instance().logger().warn("Could not create system chat packet", e);
            }
        }
    }
}
