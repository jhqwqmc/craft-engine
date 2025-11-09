package net.momirealms.craftengine.bukkit.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class BukkitCraftEnginePlugin extends JavaPlugin {
    private final BukkitCraftEngine plugin;

    public BukkitCraftEnginePlugin() {
        this.plugin = new BukkitCraftEngine(this);
        this.plugin.applyDependencies();
        this.plugin.setUpConfigAndLocale();
    }

    @Override
    public void onLoad() {
        this.plugin.onPluginLoad();
    }

    @Override
    public void onEnable() {
        this.plugin.onPluginEnable();
        this.plugin.logger().warn("You're using the CraftEngine Community Edition.");
        this.plugin.logger().warn("Please consider purchasing the premium version to support CraftEngine's development.");
    }

    @Override
    public void onDisable() {
        this.plugin.onPluginDisable();
    }
}
