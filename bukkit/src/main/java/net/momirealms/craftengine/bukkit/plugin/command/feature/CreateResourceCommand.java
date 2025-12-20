package net.momirealms.craftengine.bukkit.plugin.command.feature;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.pack.ResourceLocation;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.locale.MessageConstants;
import net.momirealms.craftengine.core.util.FileUtils;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateResourceCommand extends BukkitCommandFeature<CommandSender> {

    public CreateResourceCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(manager.flagBuilder("silent").withAliases("s"))
                .flag(manager.flagBuilder("full").withAliases("f"))
                .required("pack", StringParser.stringComponent(StringParser.StringMode.SINGLE))
                .optional("namespace", StringParser.stringComponent(StringParser.StringMode.SINGLE))
                .optional("author", StringParser.stringComponent(StringParser.StringMode.SINGLE))
                .optional("description", StringParser.stringComponent(StringParser.StringMode.QUOTED))
                .handler(context -> {
                    String packFolder = context.get("pack");
                    Path packPath = plugin().dataFolderPath().resolve("resources").resolve(packFolder);
                    Path configurationPath = packPath.resolve("configuration");
                    Path resourcepackPath = packPath.resolve("resourcepack");
                    Path packMetaPath = packPath.resolve("pack.yml");
                    if (Files.exists(packPath)) {
                        handleFeedback(context, MessageConstants.COMMAND_RESOURCE_CREATE_EXISTS, Component.text(packFolder));
                        return;
                    }
                    String namespace = context.getOrDefault("namespace", packFolder);
                    if (!ResourceLocation.isValidNamespace(namespace)) {
                        handleFeedback(context, MessageConstants.COMMAND_RESOURCE_CREATE_FAILURE_INVALID_NAMESPACE, Component.text(packFolder), Component.text(namespace));
                        return;
                    }
                    String author = context.getOrDefault("author", "CraftEngine");
                    String description = context.getOrDefault("description", "Auto-created by CraftEngine");
                    try {
                        FileUtils.createDirectoriesSafe(packPath);
                        FileUtils.createDirectoriesSafe(configurationPath);
                        Path namespacePath = resourcepackPath.resolve("assets").resolve(namespace);
                        FileUtils.createDirectoriesSafe(namespacePath);
                        if (context.flags().hasFlag("full")) {
                            Path modelsPath = namespacePath.resolve("models");
                            FileUtils.createDirectoriesSafe(modelsPath.resolve("block"));
                            FileUtils.createDirectoriesSafe(modelsPath.resolve("item"));
                            Path texturesPath = namespacePath.resolve("textures");
                            FileUtils.createDirectoriesSafe(texturesPath.resolve("block"));
                            FileUtils.createDirectoriesSafe(texturesPath.resolve("entity"));
                            FileUtils.createDirectoriesSafe(texturesPath.resolve("font"));
                            FileUtils.createDirectoriesSafe(texturesPath.resolve("gui").resolve("sprites").resolve("tooltip"));
                            FileUtils.createDirectoriesSafe(texturesPath.resolve("item"));
                            FileUtils.createDirectoriesSafe(texturesPath.resolve("trims"));
                            FileUtils.createDirectoriesSafe(namespacePath.resolve("sounds"));
                        }
                        YamlDocument document = plugin().config().loadYamlData(packMetaPath);
                        document.set("author", author);
                        document.set("version", "0.0.1");
                        document.set("description", description);
                        document.set("namespace", namespace);
                        document.set("enable", false);
                        document.save(packMetaPath.toFile());
                    } catch (IOException e) {
                        handleFeedback(context, MessageConstants.COMMAND_RESOURCE_CREATE_FAILURE, Component.text(packFolder), Component.text(e.getMessage()));
                        return;
                    }
                    handleFeedback(context, MessageConstants.COMMAND_RESOURCE_CREATE_SUCCESS, Component.text(packFolder));
                });
    }

    @Override
    public String getFeatureID() {
        return "create_resource";
    }
}
