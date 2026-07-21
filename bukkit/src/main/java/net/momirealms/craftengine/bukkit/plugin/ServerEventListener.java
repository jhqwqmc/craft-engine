package net.momirealms.craftengine.bukkit.plugin;

import net.momirealms.craftengine.bukkit.api.event.ServerPreShutdownEvent;
import net.momirealms.craftengine.bukkit.util.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class ServerEventListener implements Listener {
    private final BukkitCraftEngine plugin;
    private boolean shutdown;

    public ServerEventListener(BukkitCraftEngine plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickEnd(PluginDisableEvent event) {
        if (!ServerUtils.isRunning() && !this.shutdown) {
            this.shutdown = true;
            Bukkit.getPluginManager().callEvent(new ServerPreShutdownEvent());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPreShutdown(ServerPreShutdownEvent event) {
        this.plugin.logger().info("Disabling CraftEngine before other plugins are unloaded.");
        this.plugin.onPluginDisable();
    }
}
