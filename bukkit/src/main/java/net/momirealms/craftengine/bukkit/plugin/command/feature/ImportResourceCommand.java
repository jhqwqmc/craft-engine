package net.momirealms.craftengine.bukkit.plugin.command.feature;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ImportResourceCommand  extends BukkitCommandFeature<CommandSender> {

    public ImportResourceCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("pack", StringParser.stringComponent(StringParser.StringMode.GREEDY).suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(getSuggestions());
                    }
                }))
                /*.handler(context -> {
                    String packFolder = context.get("pack");
                    context.sender().sendMessage("你尝试导入: " + packFolder);
                })*/;
    }

    private List<Suggestion> getSuggestions() {
        Path resourcesFolder = plugin().dataFolderPath().resolve("resources");
        if (!Files.exists(resourcesFolder)) return List.of();
        try (Stream<Path> stream = Files.list(resourcesFolder)) {
            return stream.filter(Files::isDirectory)
                    .filter(this::isValidPack)
                    .map(path -> Suggestion.suggestion(path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            plugin().logger().warn("Failed to scan packs", e);
            return List.of();
        }
    }

    private boolean isValidPack(Path packPath) {
        Path yamlFile = packPath.resolve("pack.yml");
        if (!Files.exists(yamlFile)) return false;
        if (!Files.isRegularFile(yamlFile)) return false;
        YamlDocument metaYML = Config.instance().loadYamlData(yamlFile.toFile());
        boolean enable = metaYML.getBoolean("enable", true);
        if (enable) return false;
        Object export = metaYML.get("export", null);
        if (export == null) return false;
        return !plugin().packManager().loadedPacks()
                .stream().filter(Pack::enabled).map(Pack::name).toList()
                .contains(packPath.getFileName().toString());
    }

    @Override
    public String getFeatureID() {
        return "import_resource";
    }
}
