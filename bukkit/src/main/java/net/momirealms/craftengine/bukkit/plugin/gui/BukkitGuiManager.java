package net.momirealms.craftengine.bukkit.plugin.gui;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.block.entity.BlockEntityHolder;
import net.momirealms.craftengine.bukkit.block.entity.SimpleStorageBlockEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.InventoryUtils;
import net.momirealms.craftengine.bukkit.util.LegacyInventoryUtils;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.gui.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitGuiManager implements GuiManager, Listener {
    private static BukkitGuiManager instance;
    private final BukkitCraftEngine plugin;

    public BukkitGuiManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        instance = this;
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin.javaPlugin());
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void openInventory(net.momirealms.craftengine.core.entity.player.Player player, GuiType guiType) {
        Player bukkitPlayer = (Player) player.platformPlayer();
        switch (guiType) {
            case ANVIL -> LegacyInventoryUtils.openAnvil(bukkitPlayer);
            case LOOM -> LegacyInventoryUtils.openLoom(bukkitPlayer);
            case GRINDSTONE -> LegacyInventoryUtils.openGrindstone(bukkitPlayer);
            case SMITHING -> LegacyInventoryUtils.openSmithingTable(bukkitPlayer);
            case CRAFTING -> LegacyInventoryUtils.openWorkbench(bukkitPlayer);
            case ENCHANTMENT -> LegacyInventoryUtils.openEnchanting(bukkitPlayer);
            case CARTOGRAPHY -> LegacyInventoryUtils.openCartographyTable(bukkitPlayer);
        }
    }

    @Override
    public void updateInventoryTitle(net.momirealms.craftengine.core.entity.player.Player player, Component component) {
        Object nmsPlayer = player.serverPlayer();
        try {
            Object containerMenu = FastNMS.INSTANCE.field$Player$containerMenu(nmsPlayer);
            int containerId = CoreReflections.field$AbstractContainerMenu$containerId.getInt(containerMenu);
            Object menuType = CoreReflections.field$AbstractContainerMenu$menuType.get(containerMenu);
            Object packet = NetworkReflections.constructor$ClientboundOpenScreenPacket.newInstance(containerId, menuType, ComponentUtils.adventureToMinecraft(component));
            player.sendPacket(packet, false);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to update inventory title", e);
        }
    }

    @Override
    public Inventory createInventory(Gui gui, int size) {
        CraftEngineGUIHolder holder = new CraftEngineGUIHolder(gui);
        org.bukkit.inventory.Inventory inventory = FastNMS.INSTANCE.createSimpleStorageContainer(holder, size, false, false);
        holder.holder().bindValue(inventory);
        return new BukkitInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        org.bukkit.inventory.Inventory inventory = event.getInventory();
        if (!InventoryUtils.isCustomContainer(inventory)) return;
        if (!(inventory.getHolder() instanceof CraftEngineGUIHolder craftEngineGUIHolder)) {
            return;
        }
        AbstractGui gui = (AbstractGui) craftEngineGUIHolder.gui();
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == player.getInventory()) {
            gui.handleInventoryClick(new BukkitClick(event, gui, new BukkitInventory(player.getInventory())));
        } else if (event.getClickedInventory() == inventory) {
            gui.handleGuiClick(new BukkitClick(event, gui, new BukkitInventory(inventory)));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInventoryDrag(InventoryDragEvent event) {
        org.bukkit.inventory.Inventory inventory = event.getInventory();
        if (!InventoryUtils.isCustomContainer(inventory)) return;
        if (!(inventory.getHolder() instanceof CraftEngineGUIHolder)) {
            return;
        }
        for (int raw : event.getRawSlots()) {
            if (raw < inventory.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // 处理自定义容器的关闭音效
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInventoryClose(InventoryCloseEvent event) {
        org.bukkit.inventory.Inventory inventory = event.getInventory();
        if (!InventoryUtils.isCustomContainer(inventory)) return;
        if (!(inventory.getHolder() instanceof BlockEntityHolder holder)) {
            return;
        }
        if (event.getPlayer() instanceof Player player && holder.blockEntity() instanceof SimpleStorageBlockEntity simpleStorageBlockEntity) {
            simpleStorageBlockEntity.onPlayerClose(this.plugin.adapt(player));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInventoryClose(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        org.bukkit.inventory.Inventory inventory = player.getInventory();
        if (!InventoryUtils.isCustomContainer(inventory)) return;
        if (!(inventory.getHolder() instanceof BlockEntityHolder holder)) {
            return;
        }
        if (holder.blockEntity() instanceof SimpleStorageBlockEntity simpleStorageBlockEntity) {
            simpleStorageBlockEntity.onPlayerClose(this.plugin.adapt(player));
        }
    }

    public static BukkitGuiManager instance() {
        return instance;
    }
}
