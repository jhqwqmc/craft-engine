package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;

public class ItemBrowserAdminCommand extends BukkitCommandFeature<CommandSender> {

    public ItemBrowserAdminCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("players", MultiplePlayerSelectorParser.multiplePlayerSelectorParser(true))
                .handler(context -> {
                    MultiplePlayerSelector selector = context.get("players");
                    for (Player player : selector.values()) {
                        BukkitServerPlayer serverPlayer = plugin().adapt(player);
                        if (serverPlayer == null) return;
                        plugin().itemBrowserManager().open(serverPlayer);
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "item_browser_admin";
    }
}
