package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class ExportResourceCommand  extends BukkitCommandFeature<CommandSender> {

    public ExportResourceCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("pack", StringParser.stringComponent(StringParser.StringMode.GREEDY).suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(plugin().packManager().loadedPacks().stream().map(pack -> Suggestion.suggestion(pack.name())).toList());
                    }
                }))
                /*.handler(context -> {
                    String packFolder = context.get("pack");
                    Pack pack = getPack(packFolder);
                    if (pack == null) {
                        context.sender().sendMessage("找不到资源包: " + packFolder);
                        return;
                    }
                    context.sender().sendMessage("你尝试导出: " + packFolder);
                    Pair<List<Path>, List<Path>> files = FileUtils.getConfigsDeeply(pack.configurationFolder());
                })*/;
    }

    private Pack getPack(String packFolder) {
        for (Pack pack : plugin().packManager().loadedPacks()) {
            if (pack.name().equals(packFolder)) return pack;
        }
        return null;
    }

    @Override
    public String getFeatureID() {
        return "export_resource";
    }
}
