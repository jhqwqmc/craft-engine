package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.FlagKeys;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.parser.standard.DoubleParser;

public class SetEntityViewDistanceScaleCommand extends BukkitCommandFeature<CommandSender> {

    public SetEntityViewDistanceScaleCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(FlagKeys.SILENT_FLAG)
                .required("player", PlayerParser.playerParser())
                .required("scale", DoubleParser.doubleParser(0.125, 8))
                .handler(context -> {
                    if (!Config.enableEntityCulling()) {
                        context.sender().sendMessage(Component.text("Entity culling is not enabled on this server").color(NamedTextColor.RED));
                        return;
                    }
                    if (Config.entityCullingViewDistance() <= 0) {
                        context.sender().sendMessage(Component.text("View distance is not enabled on this server").color(NamedTextColor.RED));
                        return;
                    }
                    Player player = context.get("player");
                    double scale = context.get("scale");
                    BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(player);
                    serverPlayer.setEntityCullingViewDistanceScale(scale);
                    handleFeedback(context, MessageConstants.COMMAND_ENTITY_VIEW_DISTANCE_SCALE_SET_SUCCESS, Component.text(scale), Component.text(player.getName()));
                });
    }

    @Override
    public String getFeatureID() {
        return "set_entity_view_distance_scale";
    }
}
