package net.momirealms.craftengine.bukkit.compatibility;

import cn.gtemc.levelerbridge.core.BukkitLevelerBridge;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.bukkit.block.entity.renderer.element.BukkitBlockEntityElementConfigs;
import net.momirealms.craftengine.bukkit.compatibility.bedrock.FloodgateUtils;
import net.momirealms.craftengine.bukkit.compatibility.bedrock.GeyserUtils;
import net.momirealms.craftengine.bukkit.compatibility.legacy.slimeworld.LegacySlimeFormatStorageAdaptor;
import net.momirealms.craftengine.bukkit.compatibility.model.bettermodel.BetterModelBlockEntityElementConfig;
import net.momirealms.craftengine.bukkit.compatibility.model.bettermodel.BetterModelModel;
import net.momirealms.craftengine.bukkit.compatibility.model.modelengine.ModelEngineBlockEntityElementConfig;
import net.momirealms.craftengine.bukkit.compatibility.model.modelengine.ModelEngineModel;
import net.momirealms.craftengine.bukkit.compatibility.model.modelengine.ModelEngineUtils;
import net.momirealms.craftengine.bukkit.compatibility.mythicmobs.MythicItemDropListener;
import net.momirealms.craftengine.bukkit.compatibility.mythicmobs.MythicSkillHelper;
import net.momirealms.craftengine.bukkit.compatibility.packetevents.WrappedBlockStateHelper;
import net.momirealms.craftengine.bukkit.compatibility.papi.PlaceholderAPIUtils;
import net.momirealms.craftengine.bukkit.compatibility.permission.LuckPermsEventListeners;
import net.momirealms.craftengine.bukkit.compatibility.quickshop.QuickShopItemExpressionHandler;
import net.momirealms.craftengine.bukkit.compatibility.worldguard.WorldGuardRegionCondition;
import net.momirealms.craftengine.bukkit.compatibility.skript.SkriptHook;
import net.momirealms.craftengine.bukkit.compatibility.slimeworld.SlimeFormatStorageAdaptor;
import net.momirealms.craftengine.bukkit.compatibility.nameplates.CustomNameplateHatSettings;
import net.momirealms.craftengine.bukkit.compatibility.nameplates.CustomNameplateProviders;
import net.momirealms.craftengine.bukkit.compatibility.viaversion.ViaVersionUtils;
import net.momirealms.craftengine.bukkit.compatibility.worldedit.WorldEditBlockRegister;
import net.momirealms.craftengine.bukkit.font.BukkitFontManager;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.BlockManager;
import net.momirealms.craftengine.core.entity.furniture.ExternalModel;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.loot.LootConditions;
import net.momirealms.craftengine.core.plugin.compatibility.CompatibilityManager;
import net.momirealms.craftengine.core.plugin.compatibility.ModelProvider;
import net.momirealms.craftengine.core.plugin.compatibility.TagResolverProvider;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.condition.AlwaysFalseCondition;
import net.momirealms.craftengine.core.plugin.context.event.EventConditions;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.text.minimessage.FormattedLine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class BukkitCompatibilityManager implements CompatibilityManager<org.bukkit.entity.Player> {
    private final BukkitCraftEngine plugin;
    private final Map<String, ModelProvider> modelProviders;
    private final Map<String, TagResolverProvider> tagResolverProviders;
    private TagResolverProvider[] tagResolverProviderArray = null;
    private BukkitLevelerBridge levelerBridge;
    private boolean hasPlaceholderAPI;
    private boolean hasGeyser;
    private boolean hasFloodgate;

    public BukkitCompatibilityManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        this.modelProviders = new HashMap<>(Map.of(
                "ModelEngine", ModelEngineModel::new,
                "BetterModel", BetterModelModel::new
        ));
        this.tagResolverProviders = new HashMap<>();
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        this.initSlimeWorldHook();
        // WorldEdit
        // FastAsyncWorldEdit
        if (this.isPluginEnabled("FastAsyncWorldEdit")) {
            try {
                this.initFastAsyncWorldEditHook();
                logHook("FastAsyncWorldEdit");
            } catch (Exception e) {
                this.plugin.logger().warn("[Compatibility] Failed to initialize FastAsyncWorldEdit hook", e);
            }
        } else if (this.isPluginEnabled("WorldEdit")) {
            this.initWorldEditHook();
            logHook("WorldEdit");
        }
        if (this.hasPlugin("BetterModel")) {
            BukkitBlockEntityElementConfigs.register(Key.of("craftengine:better_model"), new BetterModelBlockEntityElementConfig.Factory());
            logHook("BetterModel");
        }
        if (this.hasPlugin("ModelEngine")) {
            BukkitBlockEntityElementConfigs.register(Key.of("craftengine:model_engine"), new ModelEngineBlockEntityElementConfig.Factory());
            logHook("ModelEngine");
        }
        if (this.hasPlugin("CustomNameplates")) {
            registerTagResolverProvider(new CustomNameplateProviders.Background());
            registerTagResolverProvider(new CustomNameplateProviders.Nameplate());
            registerTagResolverProvider(new CustomNameplateProviders.Bubble());
            new CustomNameplateHatSettings().register();
            logHook("CustomNameplates");
        }
        Key worldGuardRegion = Key.of("worldguard:region");
        if (this.hasPlugin("WorldGuard")) {
            EventConditions.register(worldGuardRegion, new WorldGuardRegionCondition.FactoryImpl<>());
            LootConditions.register(worldGuardRegion, new WorldGuardRegionCondition.FactoryImpl<>());
            logHook("WorldGuard");
        } else {
            EventConditions.register(worldGuardRegion, new AlwaysFalseCondition.FactoryImpl<>());
            LootConditions.register(worldGuardRegion, new AlwaysFalseCondition.FactoryImpl<>());
        }
        if (this.hasPlugin("Geyser-Spigot")) {
            this.hasGeyser = true;
        }
        if (this.hasPlugin("floodgate")) {
            this.hasFloodgate = true;
        }
    }

    @Override
    public void onDelayedEnable() {
        if (this.isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPIUtils.registerExpansions(this.plugin);
            this.hasPlaceholderAPI = true;
            logHook("PlaceholderAPI");
        }
        if (this.isPluginEnabled("LuckPerms")) {
            this.initLuckPermsHook();
            logHook("LuckPerms");
        }
        if (this.isPluginEnabled("Skript")) {
            SkriptHook.register();
            logHook("Skript");
        }
        if (this.isPluginEnabled("MythicMobs")) {
            new MythicItemDropListener(this.plugin);
            logHook("MythicMobs");
        }
        if (this.isPluginEnabled("QuickShop-Hikari")) {
            new QuickShopItemExpressionHandler(this.plugin).register();
            logHook("QuickShop-Hikari");
        }
        if (this.isPluginEnabled("packetevents")) {
            try {
                WrappedBlockStateHelper.register(null);
            } catch (Throwable e) {
                this.plugin.logger().warn("Failed to register block to WrappedBlockState", e);
            }
            logHook("packetevents");
        }
        if (this.isPluginEnabled("GrimAC")) {
            try {
                WrappedBlockStateHelper.register("ac{}grim{}grimac{}shaded{}com{}github{}retrooper{}packetevents");
            } catch (Throwable e) {
                this.plugin.logger().warn("Failed to register block to WrappedBlockState", e);
            }
            logHook("GrimAC");
        }
    }

    @Override
    public void executeMMSkill(String skill, float power, Player player) {
        MythicSkillHelper.execute(skill, power, player);
    }

    @Override
    public void registerTagResolverProvider(TagResolverProvider provider) {
        this.tagResolverProviders.put(provider.name(), provider);
        this.tagResolverProviderArray = this.tagResolverProviders.values().toArray(new TagResolverProvider[0]);
        FormattedLine.Companion.resetWithCustomResolvers(new ArrayList<>(this.tagResolverProviders.keySet()));
    }

    private void logHook(String plugin) {
        this.plugin.logger().info(TranslationManager.instance().translateLog("info.compatibility", plugin));
    }

    @Override
    public ExternalModel createModel(String plugin, String id) {
        return this.modelProviders.get(plugin).createModel(id);
    }

    @Override
    public int interactionToBaseEntity(int id) {
        return ModelEngineUtils.interactionToBaseEntity(id);
    }

    private void initLuckPermsHook() {
        new LuckPermsEventListeners(plugin.javaPlugin(), (uuid) -> {
            BukkitFontManager fontManager = plugin.fontManager();
            fontManager.refreshEmojiSuggestions(uuid);
        });
    }

    private void initSlimeWorldHook() {
        WorldManager worldManager = this.plugin.worldManager();
        if (VersionHelper.isOrAbove1_21_4()) {
            try {
                Class.forName("com.infernalsuite.asp.api.AdvancedSlimePaperAPI");
                SlimeFormatStorageAdaptor adaptor = new SlimeFormatStorageAdaptor(worldManager);
                worldManager.setStorageAdaptor(adaptor);
                Bukkit.getPluginManager().registerEvents(adaptor, plugin.javaPlugin());
                logHook("AdvancedSlimePaper");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            try {
                Class.forName("com.infernalsuite.aswm.api.SlimePlugin");
                LegacySlimeFormatStorageAdaptor adaptor = new LegacySlimeFormatStorageAdaptor(worldManager, 1);
                worldManager.setStorageAdaptor(adaptor);
                Bukkit.getPluginManager().registerEvents(adaptor, plugin.javaPlugin());
                logHook("AdvancedSlimePaper");
            } catch (ClassNotFoundException ignored) {
                if (hasPlugin("SlimeWorldPlugin")) {
                    LegacySlimeFormatStorageAdaptor adaptor = new LegacySlimeFormatStorageAdaptor(worldManager, 2);
                    worldManager.setStorageAdaptor(adaptor);
                    Bukkit.getPluginManager().registerEvents(adaptor, plugin.javaPlugin());
                    logHook("AdvancedSlimePaper");
                }
            }
        }
    }

    @SuppressWarnings({"deprecation", "DataFlowIssue"})
    private void initFastAsyncWorldEditHook() {
        Plugin fastAsyncWorldEdit = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        String version = VersionHelper.isPaper() ? fastAsyncWorldEdit.getPluginMeta().getVersion() : fastAsyncWorldEdit.getDescription().getVersion();
        if (!WorldEditBlockRegister.checkFAWECompatible(version)) {
            if (VersionHelper.isOrAbove1_20_3()) {
                this.plugin.logger().severe("");
                if (Locale.getDefault() == Locale.SIMPLIFIED_CHINESE) {
                    this.plugin.logger().severe("[兼容性] 插件需要更新 FastAsyncWorldEdit 到 2.13.0 或更高版本，以获得更好的兼容性。(当前版本: " + version + ")");
                    this.plugin.logger().severe("[兼容性] 请前往 https://ci.athion.net/job/FastAsyncWorldEdit/ 下载最新版本");
                } else {
                    this.plugin.logger().severe("[Compatibility] Update FastAsyncWorldEdit to v2.13.0+ for better compatibility (Current: " + version + ")");
                    this.plugin.logger().severe("[Compatibility] Download latest version: https://ci.athion.net/job/FastAsyncWorldEdit/");
                }
                this.plugin.logger().severe("");
            }
        }
        WorldEditBlockRegister.init(true);
    }

    private void initWorldEditHook() {
        WorldEditBlockRegister.init(false);
        try {
            for (int i = 0; i < Config.serverSideBlocks(); i++) {
                WorldEditBlockRegister.register(BlockManager.createCustomBlockKey(i));
            }
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to initialize world edit hook", e);
        }
    }

    private Plugin getPlugin(String name) {
        return Bukkit.getPluginManager().getPlugin(name);
    }

    @Override
    public boolean hasPlaceholderAPI() {
        return this.hasPlaceholderAPI;
    }

    @Override
    public boolean isPluginEnabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

    @Override
    public boolean hasPlugin(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    @Override
    public String parse(Player player, String text) {
        return player == null
                ? PlaceholderAPIUtils.parse(null, text)
                : PlaceholderAPIUtils.parse((org.bukkit.entity.Player) player.platformPlayer(), text);
    }

    @Override
    public String parse(Player player1, Player player2, String text) {
        return PlaceholderAPIUtils.parse((org.bukkit.entity.Player) player1.platformPlayer(), (org.bukkit.entity.Player) player2.platformPlayer(), text);
    }

    @Override
    public int getViaVersionProtocolVersion(NetWorkUser user) {
        return ViaVersionUtils.getPlayerProtocolVersion(user);
    }

    @Override
    public TagResolver[] createExternalTagResolvers(Context context) {
        if (this.tagResolverProviderArray == null) return null;
        int length = this.tagResolverProviderArray.length;
        TagResolver[] resolvers = new TagResolver[length];
        for (int i = 0; i < length; i++) {
            resolvers[i] = this.tagResolverProviderArray[i].getTagResolver(context);
        }
        return resolvers;
    }

    @Override
    public boolean isBedrockPlayer(Player player) {
        UUID uuid = player.uuid();
        if (this.hasFloodgate) {
            return FloodgateUtils.isFloodgatePlayer(uuid);
        }
        if (this.hasGeyser) {
            return GeyserUtils.isGeyserPlayer(uuid);
        }
        return uuid.version() == 0;
    }

    @Override
    public BukkitLevelerBridge levelerBridge() {
        if (this.levelerBridge == null) {
            this.levelerBridge = BukkitLevelerBridge.builder().build();
        }
        return this.levelerBridge;
    }
}
