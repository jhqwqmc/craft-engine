package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PackPreferenceCommand extends BukkitCommandFeature<CommandSender> {
    public static final String PACK_PERMISSION_PREFIX = "ce.resourcepack.";
    public static final String ADMIN_PERMISSION = "ce.command.admin.pack_preference";

    public PackPreferenceCommand(CraftEngineCommandManager<CommandSender> manager, CraftEngine plugin) {
        super(manager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("action", EnumParser.enumParser(Action.class))
                .required("pack", StringParser.stringParser(), (context, input) -> CompletableFuture.completedFuture(
                        plugin().packManager().resourcePackHosts().keySet().stream()
                                .filter(id -> context.sender().hasPermission(PACK_PERMISSION_PREFIX + id) || context.sender().hasPermission(ADMIN_PERMISSION))
                                .map(Suggestion::suggestion).toList()))
                .optional("player", StringParser.stringParser())
                .handler(context -> {
                    String pack = context.get("pack");
                    Action action = context.get("action");
                    String target = context.getOrDefault("player", null);
                    CommandSender sender = context.sender();
                    if (sender instanceof Player && !sender.hasPermission(PACK_PERMISSION_PREFIX + pack)) {
                        handleFeedback(context, MessageConstants.COMMAND_PACK_PREFERENCE_NO_PERMISSION, Component.text(pack));
                        return;
                    }
                    Boolean enabled = switch (action) {
                        case ENABLE -> true;
                        case DISABLE -> false;
                        case RESET -> null;
                    };
                    UUID playerId;
                    if (target == null) {
                        if (!(sender instanceof Player player)) throw new IllegalArgumentException("Console must specify an online player name or UUID");
                        playerId = player.getUniqueId();
                    } else {
                        Player player = Bukkit.getPlayerExact(target);
                        playerId = player == null ? UUID.fromString(target) : player.getUniqueId();
                    }
                    plugin().packManager().setPackPreference(playerId, pack, enabled).whenComplete((ignored, error) -> {
                        if (error != null) {
                            plugin().logger().warn("Failed to save resource pack preference for " + playerId, error);
                        }
                    });
                });
    }

    @Override
    public String getFeatureID() {
        return "pack_preference";
    }

    public enum Action {
        ENABLE,
        DISABLE,
        RESET
    }
}
