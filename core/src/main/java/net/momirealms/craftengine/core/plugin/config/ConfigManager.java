package net.momirealms.craftengine.core.plugin.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.pack.conflict.resolution.ConditionalResolution;
import net.momirealms.craftengine.core.pack.host.HostMode;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.PluginProperties;
import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ReflectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager implements Reloadable {
    private static ConfigManager instance;
    protected final CraftEngine plugin;
    private final Path configFilePath;
    private final String configVersion;
    private YamlDocument config;

    protected boolean debug;
    protected boolean checkUpdate;
    protected boolean metrics;
    protected boolean resourcePack$overrideUniform;
    protected boolean resourcePack$protection$breakFormat;
    protected boolean hasPAPI;
    protected boolean forceUpdateLight;
    protected boolean enableLightSystem;
    protected int maxChainUpdate;
    protected boolean removeInvalidFurniture;
    protected Set<String> furnitureToRemove;
    protected float packMinVersion;
    protected float packMaxVersion;
    protected boolean enableSoundSystem;
    protected boolean enableRecipeSystem;
    protected List<String> foldersToMerge;
	protected boolean restoreVanillaBlocks;
	protected boolean restoreCustomBlocks;
    protected HostMode hostMode;
    protected int hostPort;
    protected String hostIP;
    protected String hostProtocol;
    protected boolean kickDeclined;
    protected boolean sendPackOnJoin;
    protected boolean sendPackOnReload;
    protected boolean denyNonMinecraftRequest;
    protected Component resourcePackPrompt;
    protected String packUrl;
    protected String packSha1;
    protected String hostResourcePackPath;
    protected UUID packUUID;
    protected int requestRate;
    protected long requestInterval;
    protected List<ConditionalResolution> resolutions;
    protected boolean nonItalic;

    public ConfigManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.configVersion = PluginProperties.getValue("config");
        this.configFilePath = this.plugin.dataFolderPath().resolve("config.yml");
        instance = this;
    }

    @Override
    public void load() {
        if (Files.exists(this.configFilePath)) {
            this.config = this.loadYamlData(this.configFilePath.toFile());
            String configVersion = config.getString("config-version");
            if (configVersion.equals(this.configVersion)) {
                loadSettings();
                return;
            }
        }
        this.updateConfigVersion();
        loadSettings();
    }

    private void updateConfigVersion() {
        this.config = this.loadYamlConfig(
                "config.yml",
                GeneralSettings.builder()
                        .setRouteSeparator('.')
                        .setUseDefaults(false)
                        .build(),
                LoaderSettings
                        .builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.builder()
                        .setEscapeUnprintable(false)
                        .setScalarFormatter((tag, value, role, def) -> {
                            if (role == NodeRole.KEY) {
                                return ScalarStyle.PLAIN;
                            } else {
                                return tag == Tag.STR ? ScalarStyle.DOUBLE_QUOTED : ScalarStyle.PLAIN;
                            }
                        })
                        .build(),
                UpdaterSettings
                        .builder()
                        .setVersioning(new BasicVersioning("config-version"))
                        .build()
        );
        try {
            config.save(new File(plugin.dataFolderFile(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unload() {
        Reloadable.super.unload();
    }

    private void loadSettings() {
        YamlDocument config = settings();
        plugin.translationManager().forcedLocale(TranslationManager.parseLocale(config.getString("forced-locale", "")));
        // Basics
        debug = config.getBoolean("debug", false);
        metrics = config.getBoolean("metrics", false);
        checkUpdate = config.getBoolean("update-checker", false);
        // resource pack
        resourcePack$overrideUniform = config.getBoolean("resource-pack.override-uniform-font", false);
        packMinVersion = getVersion(config.get("resource-pack.supported-version.min", "1.20").toString());
        packMaxVersion = getVersion(config.get("resource-pack.supported-version.max", "LATEST").toString());
        foldersToMerge = config.getStringList("resource-pack.merge-external-folders");
        hostMode = HostMode.valueOf(config.getString("resource-pack.send.mode", "self-host").replace("-", "_").toUpperCase(Locale.ENGLISH));
        hostPort = config.getInt("resource-pack.send.self-host.port", 8163);
        hostIP = config.getString("resource-pack.send.self-host.ip", "localhost");
        hostResourcePackPath = config.getString("resource-pack.send.self-host.local-file-path", "./generated/resource_pack.zip");
        hostProtocol = config.getString("resource-pack.send.self-host.protocol", "http");
        sendPackOnJoin = config.getBoolean("resource-pack.send.send-on-join", true);
        sendPackOnReload = config.getBoolean("resource-pack.send.send-on-reload", true);
        kickDeclined = config.getBoolean("resource-pack.send.kick-if-declined", true);
        packUrl = config.getString("resource-pack.send.external-host.url", "");
        packSha1 = config.getString("resource-pack.send.external-host.sha1", "");
        String packUUIDStr = config.getString("resource-pack.send.external-host.uuid", "");
        packUUID = packUUIDStr.isEmpty() ? UUID.nameUUIDFromBytes(packUrl.getBytes(StandardCharsets.UTF_8)) : UUID.fromString(packUUIDStr);
        resourcePackPrompt = AdventureHelper.miniMessage(config.getString("resource-pack.send.prompt", "<yellow>To fully experience our server, please accept our custom resource pack.</yellow>"));
        requestInterval = config.getLong("resource-pack.send.self-host.rate-limit.reset-interval", 30L);
        requestRate = config.getInt("resource-pack.send.self-host.rate-limit.max-requests", 3);
        denyNonMinecraftRequest = config.getBoolean("resource-pack.send.deny-non-minecraft-request", true);
        try {
            resolutions = config.getMapList("resource-pack.duplicated-files-handler").stream().map(it -> {
                Map<String, Object> args = MiscUtils.castToMap(it, false);
                return ConditionalResolution.FACTORY.create(args);
            }).toList();
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to load resource pack duplicated files handler", e);
            resolutions = List.of();
        }

        // item
        nonItalic = config.getBoolean("item.non-italic-tag", false);

        // performance
        maxChainUpdate = config.getInt("performance.max-block-chain-update-limit", 64);
        forceUpdateLight = config.getBoolean("performance.light-system.force-update-light", false);
        enableLightSystem = config.getBoolean("performance.light-system.enable", true);
        restoreVanillaBlocks = config.getBoolean("performance.chunk-system.restore-vanilla-blocks-on-chunk-unload", true);
        restoreCustomBlocks = config.getBoolean("performance.chunk-system.restore-custom-blocks-on-chunk-load", true);
        // compatibility
        hasPAPI = plugin.isPluginEnabled("PlaceholderAPI");
        // furniture
        removeInvalidFurniture = config.getBoolean("furniture.remove-invalid-furniture-on-chunk-load.enable", false);
        furnitureToRemove = new HashSet<>(config.getStringList("furniture.remove-invalid-furniture-on-chunk-load.list"));
        // block
        enableSoundSystem = config.getBoolean("block.sound-system.enable", true);
        // recipe
        enableRecipeSystem = config.getBoolean("recipe.enable", true);

        Class<?> modClazz = ReflectionUtils.getClazz(CraftEngine.MOD_CLASS);
        if (modClazz != null) {
            Method setMaxChainMethod = ReflectionUtils.getStaticMethod(modClazz, new String[] {"setMaxChainUpdate"}, void.class, int.class);
            try {
                assert setMaxChainMethod != null;
                setMaxChainMethod.invoke(null, maxChainUpdate);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.logger().warn("Failed to set max chain update", e);
            }
        }
    }

    private static float getVersion(String version) {
        if (version.equalsIgnoreCase("LATEST")) {
            version = PluginProperties.getValue("latest-version");
        }
        String[] split = version.split("\\.", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        return Float.parseFloat(split[1]);
    }

    public static String configVersion() {
        return instance.configVersion;
    }

    public static boolean debug() {
        return instance.debug;
    }

    public static boolean checkUpdate() {
        return instance.checkUpdate;
    }

    public static boolean metrics() {
        return instance.metrics;
    }

    public static boolean hasPAPI() {
        return instance.hasPAPI;
    }

    public static boolean resourcePack$overrideUniform() {
        return instance.resourcePack$overrideUniform;
    }

    public static boolean resourcePack$protection$breakFormat() {
        return instance.resourcePack$protection$breakFormat;
    }

    public static int maxChainUpdate() {
        return instance.maxChainUpdate;
    }

    public static boolean removeInvalidFurniture() {
        return instance.removeInvalidFurniture;
    }

    public static Set<String> furnitureToRemove() {
        return instance.furnitureToRemove;
    }

    public static boolean forceUpdateLight() {
        return instance.forceUpdateLight;
    }

    public static boolean enableLightSystem() {
        return instance.enableLightSystem;
    }

    public static float packMinVersion() {
        return instance.packMinVersion;
    }

    public static float packMaxVersion() {
        return instance.packMaxVersion;
    }

    public static boolean enableSoundSystem() {
        return instance.enableSoundSystem;
    }

    public static boolean enableRecipeSystem() {
        return instance.enableRecipeSystem;
    }

    public static boolean nonItalic() {
        return instance.nonItalic;
    }

    public static boolean restoreVanillaBlocks() {
        return instance.restoreVanillaBlocks && instance.restoreCustomBlocks;
    }

    public static boolean denyNonMinecraftRequest() {
        return instance.denyNonMinecraftRequest;
    }

    public static boolean restoreCustomBlocks() {
        return instance.restoreCustomBlocks;
    }

    public static List<String> foldersToMerge() {
        return instance.foldersToMerge;
    }

    public static HostMode hostMode() {
        return instance.hostMode;
    }

    public static String hostIP() {
        return instance.hostIP;
    }

    public static int hostPort() {
        return instance.hostPort;
    }

    public static boolean kickOnDeclined() {
        return instance.kickDeclined;
    }

    public static Component resourcePackPrompt() {
        return instance.resourcePackPrompt;
    }

    public static String hostProtocol() {
        return instance.hostProtocol;
    }

    public static String externalPackUrl() {
        return instance.packUrl;
    }

    public static String externalPackSha1() {
        return instance.packSha1;
    }

    public static UUID externalPackUUID() {
        return instance.packUUID;
    }

    public static boolean sendPackOnJoin() {
        return instance.sendPackOnJoin;
    }

    public static boolean sendPackOnReload() {
        return instance.sendPackOnReload;
    }

    public static int requestRate() {
        return instance.requestRate;
    }

    public static long requestInterval() {
        return instance.requestInterval;
    }

    public static String hostResourcePackPath() {
        return instance.hostResourcePackPath;
    }

    public static List<ConditionalResolution> resolutions() {
        return instance.resolutions;
    }

    public YamlDocument loadOrCreateYamlData(String fileName) {
        File file = new File(this.plugin.dataFolderFile(), fileName);
        if (!file.exists()) {
            this.plugin.saveResource(fileName);
        }
        return this.loadYamlData(file);
    }

    public YamlDocument loadYamlConfig(String filePath, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) {
        try (InputStream inputStream = new FileInputStream(resolveConfig(filePath).toFile())) {
            return YamlDocument.create(inputStream, this.plugin.resourceStream(filePath), generalSettings, loaderSettings, dumperSettings, updaterSettings);
        } catch (IOException e) {
            this.plugin.logger().severe("Failed to load config " + filePath, e);
            return null;
        }
    }

    public YamlDocument loadYamlData(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return YamlDocument.create(inputStream);
        } catch (IOException e) {
            this.plugin.logger().severe("Failed to load config " + file, e);
            return null;
        }
    }

    public Path resolveConfig(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
        filePath = filePath.replace('\\', '/');
        Path configFile = this.plugin.dataFolderPath().resolve(filePath);
        // if the config doesn't exist, create it based on the template in the resources dir
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException ignored) {
            }
            try (InputStream is = this.plugin.resourceStream(filePath)) {
                if (is == null) {
                    throw new IllegalArgumentException("The embedded resource '" + filePath + "' cannot be found");
                }
                Files.copy(is, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return configFile;
    }

    public YamlDocument settings() {
        if (config == null) {
            throw new IllegalStateException("Main config not loaded");
        }
        return config;
    }

    public static ConfigManager instance() {
        return instance;
    }
}
