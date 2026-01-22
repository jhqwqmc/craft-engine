package net.momirealms.craftengine.bukkit.advancement;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistryOps;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.LegacyDFUUtils;
import net.momirealms.craftengine.core.advancement.AbstractAdvancementManager;
import net.momirealms.craftengine.core.advancement.AdvancementType;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.config.IdSectionConfigParser;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.nio.file.Path;
import java.util.*;

public final class BukkitAdvancementManager extends AbstractAdvancementManager {
    private final BukkitCraftEngine plugin;
    private final AdvancementParser advancementParser;
    private final Map<Key, Object> advancements = new HashMap<>();

    public BukkitAdvancementManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
        this.advancementParser = new AdvancementParser();
    }

    public void unload() {
        this.advancements.clear();
    }

    @Override
    public ConfigParser parser() {
        return this.advancementParser;
    }

    @Override
    public void runDelayedSyncTasks() {
        if (this.advancements.isEmpty()) {
            return;
        }
        Map<Object, Object> advancements = new HashMap<>();
        for (Map.Entry<Key, Object> entry : this.advancements.entrySet()) {
            Object identifier = KeyUtils.toResourceLocation(entry.getKey());
            try {
                Object advancementHolder = CoreReflections.constructor$AdvancementHolder.newInstance(identifier, entry.getValue());
                advancements.put(identifier, advancementHolder);
            } catch (ReflectiveOperationException e) {
                this.plugin.logger().warn("Failed to create advancement holder", e);
            }
        }
        try {
            Object serverAdvancementManager = CoreReflections.method$MinecraftServer$getAdvancements.invoke(FastNMS.INSTANCE.method$MinecraftServer$getServer());
            // 进度管理器里新的map
            Map<Object, Object> mapBuilder = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<Object, Object> serverAdvancementManager$advancements = (Map<Object, Object>) CoreReflections.field$ServerAdvancementManager$advancements.get(serverAdvancementManager);
            mapBuilder.putAll(serverAdvancementManager$advancements);
            mapBuilder.putAll(advancements);
            CoreReflections.field$ServerAdvancementManager$advancements.set(serverAdvancementManager, mapBuilder);
            Object advancementTree = CoreReflections.method$ServerAdvancementManager$tree.invoke(serverAdvancementManager);
            CoreReflections.method$AdvancementTree$addAll.invoke(advancementTree, advancements.values());
            HashSet<Object> processedRoots = new HashSet<>();
            for (Map.Entry<Object, Object> entry : advancements.entrySet()) {
                Object node = CoreReflections.method$AdvancementTree$get.invoke(advancementTree, entry.getKey());
                if (node != null) {
                    Object root = CoreReflections.method$AdvancementNode$root.invoke(node);
                    if (processedRoots.add(root)) {
                        Object advancementHolder = CoreReflections.method$AdvancementNode$holder.invoke(root);
                        Object advancement = CoreReflections.field$AdvancementHolder$value.get(advancementHolder);
                        Optional<?> optionalDisplay = (Optional<?>) CoreReflections.field$Advancement$display.get(advancement);
                        if (optionalDisplay.isPresent()) {
                            CoreReflections.method$TreeNodePosition$run.invoke(null, root);
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to register advancements", e);
        }
    }

    @Override
    public void sendToast(Player player, Item<?> icon, Component message, AdvancementType type) {
        try {
            Object displayInfo = CoreReflections.constructor$DisplayInfo.newInstance(
                    icon.getLiteralObject(),
                    ComponentUtils.adventureToMinecraft(message),  // title
                    CoreReflections.instance$Component$empty, // description
                    VersionHelper.isOrAbove1_20_3() ? Optional.empty() : null, // background
                    CoreReflections.instance$AdvancementType$values[type.ordinal()],
                    true, // show toast
                    false, // announce to chat
                    true // hidden
            );
            if (VersionHelper.isOrAbove1_20_2()) {
                displayInfo = Optional.of(displayInfo);
            }
            Object resourceLocation = KeyUtils.toResourceLocation(Key.of("craftengine", "toast"));
            Object criterion = VersionHelper.isOrAbove1_20_2() ?
                    CoreReflections.constructor$Criterion.newInstance(CoreReflections.constructor$ImpossibleTrigger.newInstance(), CoreReflections.constructor$ImpossibleTrigger$TriggerInstance.newInstance()) :
                    CoreReflections.constructor$Criterion.newInstance(CoreReflections.constructor$ImpossibleTrigger$TriggerInstance.newInstance());
            Map<String, Object> criteria = Map.of("impossible", criterion);
            Object advancementProgress = CoreReflections.constructor$AdvancementProgress.newInstance();
            Object advancement;
            if (VersionHelper.isOrAbove1_20_2()) {
                Object advancementRequirements = VersionHelper.isOrAbove1_20_3() ?
                        CoreReflections.constructor$AdvancementRequirements.newInstance(List.of(List.of("impossible"))) :
                        CoreReflections.constructor$AdvancementRequirements.newInstance((Object) new String[][] {{"impossible"}});
                advancement = CoreReflections.constructor$Advancement.newInstance(
                        Optional.empty(),
                        displayInfo,
                        CoreReflections.instance$AdvancementRewards$EMPTY,
                        criteria,
                        advancementRequirements,
                        false
                );
                CoreReflections.method$AdvancementProgress$update.invoke(advancementProgress, advancementRequirements);
                advancement = CoreReflections.constructor$AdvancementHolder.newInstance(resourceLocation, advancement);
            } else {
                advancement = CoreReflections.constructor$Advancement.newInstance(
                        resourceLocation,
                        null, // parent
                        displayInfo,
                        CoreReflections.instance$AdvancementRewards$EMPTY,
                        criteria,
                        new String[][] {{"impossible"}},
                        false
                );
                CoreReflections.method$AdvancementProgress$update.invoke(advancementProgress, criteria, new String[][] {{"impossible"}});
            }
            CoreReflections.method$AdvancementProgress$grantProgress.invoke(advancementProgress, "impossible");
            Map<Object, Object> advancementsToGrant = new HashMap<>();
            advancementsToGrant.put(resourceLocation, advancementProgress);
            Object grantPacket = VersionHelper.isOrAbove1_21_5() ?
                    NetworkReflections.constructor$ClientboundUpdateAdvancementsPacket.newInstance(false, Arrays.asList(advancement), new HashSet<>(), advancementsToGrant, true) :
                    NetworkReflections.constructor$ClientboundUpdateAdvancementsPacket.newInstance(false, Arrays.asList(advancement), new HashSet<>(), advancementsToGrant);
            Object removePacket = VersionHelper.isOrAbove1_21_5() ?
                    NetworkReflections.constructor$ClientboundUpdateAdvancementsPacket.newInstance(false, new ArrayList<>(), MiscUtils.init(new HashSet<>(), s -> s.add(resourceLocation)), new HashMap<>(), true) :
                    NetworkReflections.constructor$ClientboundUpdateAdvancementsPacket.newInstance(false, new ArrayList<>(), MiscUtils.init(new HashSet<>(), s -> s.add(resourceLocation)), new HashMap<>());
            player.sendPackets(List.of(grantPacket, removePacket), false);
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to send toast for player " + player.name(), e);
        }
    }

    public class AdvancementParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"advancements", "advancement"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.ADVANCEMENT;
        }

        @Override
        public int count() {
            return BukkitAdvancementManager.this.advancements.size();
        }

        @Override
        public void parseSection(Pack pack, Path path, String node, Key id, Map<String, Object> section) {
            if (BukkitAdvancementManager.this.advancements.containsKey(id)) {
                throw new LocalizedResourceConfigException("warning.config.advancement.duplicate", path, id);
            }
            JsonElement json = GsonHelper.get().toJsonTree(section);
            Object advancement = null;
            if (VersionHelper.isOrAbove1_20_5()) {
                advancement = CoreReflections.instance$Advancement$CODEC.parse(MRegistryOps.JSON, json)
                        .resultOrPartial(error -> {
                            throw new LocalizedResourceConfigException("warning.config.advancement.invalid_advancement", json.toString(), error);
                        })
                        .orElse(null);
            } else {
                advancement = LegacyDFUUtils.parse(CoreReflections.instance$Advancement$CODEC, MRegistryOps.JSON, json, (error) -> {
                    throw new LocalizedResourceConfigException("warning.config.advancement.invalid_advancement", json.toString(), error);
                });
            }
            if (advancement == null) {
                return;
            }
            BukkitAdvancementManager.this.advancements.put(id, advancement);
        }
    }
}
