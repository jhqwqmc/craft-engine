package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.font.BitmapImage;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.util.FormatUtils;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class DebugImageCommand extends BukkitCommandFeature<CommandSender> {

    public DebugImageCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().fontManager().cachedImagesSuggestions());
                    }
                }))
                .optional("row", IntegerParser.integerParser(0))
                .optional("column", IntegerParser.integerParser(0))
                .handler(context -> {
                    Key imageId = KeyUtils.namespacedKey2Key(context.get("id"));
                    plugin().fontManager().bitmapImageByImageId(imageId).ifPresent(image -> {
                        int row = context.getOrDefault("row", 0);
                        int column = context.getOrDefault("column", 0);
                        String string = image.isValidCoordinate(row, column)
                                ? imageId.asString() + ((row != 0 || column != 0) ? ":" + row + ":" + column : "") // 自动最小化
                                : imageId.asString() + ":" + (row = 0) + ":" + (column = 0); // 因为是无效的所以说要强调告诉获取的是00
                        Component component = Component.text()
                                .append(Component.text(string)
                                        .hoverEvent(image.componentAt(row, column).color(NamedTextColor.WHITE))
                                        .clickEvent(ClickEvent.suggestCommand(string)))
                                .append(getHelperInfo(image, row, column))
                                .build();
                        plugin().senderFactory().wrap(context.sender()).sendMessage(component);
                    });
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_image";
    }

    private static TextComponent.Builder getHelperInfo(BitmapImage image, int row, int column) {
        String raw = new String(Character.toChars(image.codepointAt(row, column)));
        String font = image.font().toString();
        return Component.text()
                .append(Component.text(" "))
                .append(Component.text("[MM]")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(Component.text("Copy", NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.suggestCommand(FormatUtils.miniMessageFont(raw, font))))
                .append(Component.text(" "))
                .append(Component.text("[MD]")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(Component.text("Copy", NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.suggestCommand(FormatUtils.mineDownFont(raw, font))))
                .append(Component.text(" "))
                .append(Component.text("[RAW]")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(Component.text("Copy", NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.suggestCommand(raw)));
    }
}
