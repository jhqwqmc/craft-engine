package net.momirealms.craftengine.bukkit.plugin.command.debug;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.sender.Sender;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.data.SinglePlayerSelector;
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;

import java.util.Map;

public final class DebugPackStatesCommand extends BukkitCommandFeature<CommandSender> {

    public DebugPackStatesCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("player", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                .handler(context -> {
                    SinglePlayerSelector selector = context.get("player");
                    BukkitServerPlayer player = BukkitAdaptor.adapt(selector.single());
                    if (player == null) return;
                    Sender sender = plugin().senderFactory().wrap(context.sender());
                    sender.sendMessage(DebugCommandOutput.title("Pack States"));
                    sender.sendMessage(DebugCommandOutput.value("Player", player.name()));
                    Map<String, Boolean> states = plugin().packManager().packPreferences(player);
                    if (states == null) {
                        sender.sendMessage(DebugCommandOutput.warning("Pack states have not loaded yet"));
                        return;
                    }
                    if (plugin().packManager().resourcePackHosts().isEmpty()) {
                        sender.sendMessage(DebugCommandOutput.empty(1));
                        return;
                    }
                    for (String pack : plugin().packManager().resourcePackHosts().keySet()) {
                        Boolean enabled = states.get(pack);
                        sender.sendMessage(DebugCommandOutput.value(pack, enabled == null ? "default" : enabled ? "enabled" : "disabled"));
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_pack_states";
    }
}
