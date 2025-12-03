package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.entity.furniture.AnchorType;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.FlagKeys;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.bukkit.parser.location.LocationParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class DebugSpawnFurnitureCommand extends BukkitCommandFeature<CommandSender> {

    public DebugSpawnFurnitureCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("location", LocationParser.locationParser())
                .required("id", NamespacedKeyParser.namespacedKeyComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().furnitureManager().cachedSuggestions());
                    }
                }))
                .optional("anchor-type", EnumParser.enumParser(AnchorType.class))
                .flag(FlagKeys.SILENT_FLAG)
                .handler(context -> {
                    // fixme 指令
//                    NamespacedKey namespacedKey = context.get("id");
//                    Key id = KeyUtils.namespacedKey2Key(namespacedKey);
//                    BukkitFurnitureManager furnitureManager = BukkitFurnitureManager.instance();
//                    Optional<FurnitureConfig> optionalCustomFurniture = furnitureManager.furnitureById(id);
//                    if (optionalCustomFurniture.isEmpty()) {
//                        return;
//                    }
//                    Location location = context.get("location");
//                    FurnitureConfig customFurniture = optionalCustomFurniture.get();
//                    AnchorType anchorType = (AnchorType) context.optional("anchor-type").orElse(customFurniture.getAnyAnchorType());
//                    boolean playSound = context.flags().hasFlag("silent");
//                    CraftEngineFurniture.place(location, customFurniture, anchorType, playSound);
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_spawn_furniture";
    }
}
