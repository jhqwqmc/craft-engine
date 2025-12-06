package net.momirealms.craftengine.bukkit.plugin.command.feature;

import com.google.gson.*;
import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.util.FileUtils;
import net.momirealms.craftengine.core.util.GsonHelper;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.StringParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Consumer;

public class DebugGenerateInternalAssetsCommand extends BukkitCommandFeature<CommandSender> {

    public DebugGenerateInternalAssetsCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("path", StringParser.stringParser())
                .handler(context -> {
                    // 这里指向的完整的minecraft原版资源包文件夹路径
                    String pathName = context.get("path");
                    Path resourcePackPath = this.plugin().dataFolderPath().resolve(pathName);
                    if (!Files.exists(resourcePackPath)) {
                        context.sender().sendMessage("Could not find path: " + resourcePackPath);
                        return;
                    }
                    Path assetsPath = resourcePackPath.resolve("assets");
                    Path internalPath = resourcePackPath.resolve("internal");
                    if (!Files.exists(assetsPath)) {
                        context.sender().sendMessage("Could not find path: " + assetsPath);
                        return;
                    }
                    Path minecraftNamespacePath = assetsPath.resolve("minecraft");
                    if (!Files.exists(minecraftNamespacePath)) {
                        context.sender().sendMessage("Could not find path: " + minecraftNamespacePath);
                        return;
                    }

                    // 复制atlas
                    {
                        Path atlasPath = minecraftNamespacePath.resolve("atlases").resolve("blocks.json");
                        Path assetsAtlasPath = internalPath.resolve("atlases").resolve("blocks.json");
                        try {
                            Files.createDirectories(assetsAtlasPath.getParent());
                            Files.copy(atlasPath, assetsAtlasPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            plugin().logger().warn("Failed to copy atlas file", e);
                        }
                    }

                    // 复制sounds
                    {
                        Path soundPath = minecraftNamespacePath.resolve("sounds.json");
                        if (Files.exists(soundPath)) {
                            Path targetSoundPath = internalPath.resolve("sounds.json");
                            try {
                                Files.createDirectories(targetSoundPath.getParent());
                                Files.copy(soundPath, targetSoundPath, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                plugin().logger().warn("Failed to create internal sounds file", e);
                            }
                        }
                    }

                    // 复制items
                    {
                        Path allPath = minecraftNamespacePath.resolve("items").resolve("_all.json");
                        Path targetAllPath = internalPath.resolve("items").resolve("_all.json");
                        try {
                            if (Files.exists(allPath)) {
                                Files.createDirectories(targetAllPath.getParent());
                                Files.copy(allPath, targetAllPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            plugin().logger().warn("Failed to create internal items file", e);
                        }
                    }

                    // 复制models
                    {
                        for (String name : List.of("block", "item")) {
                            Path allPath = minecraftNamespacePath.resolve("models").resolve(name).resolve("_all.json");
                            Path targetAllPath = internalPath.resolve("models").resolve(name).resolve("_all.json");
                            try {
                                if (Files.exists(allPath)) {
                                    Files.createDirectories(targetAllPath.getParent());
                                    Files.copy(allPath, targetAllPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                plugin().logger().warn("Failed to create internal models file", e);
                            }
                        }
                    }

                    // 收集textures
                    JsonArray allTextures = new JsonArray();
                    collectListJson(minecraftNamespacePath.resolve("textures"), "", allTextures::add);
                    try {
                        Path resolve = internalPath.resolve("textures/processed.json");
                        Files.createDirectories(resolve.getParent());
                        GsonHelper.writeJsonFile(allTextures, resolve);
                    } catch (IOException e) {
                        plugin().logger().warn("Failed to collect textures", e);
                    }

                    // 收集sounds
                    JsonArray allSounds = new JsonArray();
                    collectListJson(minecraftNamespacePath.resolve("sounds"), "", allSounds::add);
                    try {
                        Path resolve = internalPath.resolve("sounds/processed.json");
                        Files.createDirectories(resolve.getParent());
                        GsonHelper.writeJsonFile(allSounds, resolve);
                    } catch (IOException e) {
                        plugin().logger().warn("Failed to collect textures", e);
                    }

                    context.sender().sendMessage("Done");
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_generate_internal_assets";
    }

    private void collectListJson(Path folder, String prefix, Consumer<String> callback) {
        try (InputStream inputStream = Files.newInputStream(folder.resolve("_list.json"))) {
            String s = prefix.isEmpty() ? "" : (prefix + "/");
            JsonObject listJson = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray fileList = listJson.getAsJsonArray("files");
            for (JsonElement element : fileList) {
                if (element instanceof JsonPrimitive primitive) {
                    callback.accept(s + FileUtils.pathWithoutExtension(primitive.getAsString()));
                }
            }
            JsonArray directoryList = listJson.getAsJsonArray("directories");
            for (JsonElement element : directoryList) {
                if (element instanceof JsonPrimitive primitive) {
                    collectListJson(folder.resolve(primitive.getAsString()), s + primitive.getAsString(), callback);
                }
            }
        } catch (IOException e) {
            this.plugin().logger().warn("Failed to load _list.json" + folder, e);
        }
    }
}
