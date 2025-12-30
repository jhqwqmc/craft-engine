package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.FlagKeys;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SetLocaleCommand extends BukkitCommandFeature<CommandSender> {

    public SetLocaleCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(FlagKeys.SILENT_FLAG)
                .required("player", PlayerParser.playerParser())
                .required("locale", StringParser.stringComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(TranslationManager.ALL_LANG_SUGGESTIONS);
                    }
                }))
                .handler(context -> {
                    Player player = context.get("player");
                    String localeName = context.get("locale");
                    Locale locale = TranslationManager.parseLocale(localeName);
                    if (locale == null) {
                        handleFeedback(context, MessageConstants.COMMAND_LOCALE_SET_FAILURE, Component.text(localeName));
                        return;
                    }
                    BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(player);
                    if (serverPlayer == null) return;
                    serverPlayer.setSelectedLocale(locale);
                    handleFeedback(context, MessageConstants.COMMAND_LOCALE_SET_SUCCESS, Component.text(TranslationManager.formatLocale(locale)), Component.text(player.getName()));
                });
    }

    @Override
    public String getFeatureID() {
        return "set_locale";
    }
}
