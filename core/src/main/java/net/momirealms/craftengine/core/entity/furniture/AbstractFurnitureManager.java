package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigs;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxTypes;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.PendingConfigSection;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.IdSectionConfigParser;
import net.momirealms.craftengine.core.plugin.context.event.EventFunctions;
import net.momirealms.craftengine.core.plugin.entityculling.CullingData;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.incendo.cloud.suggestion.Suggestion;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractFurnitureManager implements FurnitureManager {
    protected final Map<Key, FurnitureConfig> byId = new HashMap<>();
    private final CraftEngine plugin;
    private final FurnitureParser furnitureParser;
    // Cached command suggestions
    private final List<Suggestion> cachedSuggestions = new ArrayList<>();

    public AbstractFurnitureManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.furnitureParser = new FurnitureParser();
    }

    @Override
    public FurnitureParser parser() {
        return this.furnitureParser;
    }

    @Override
    public void delayedLoad() {
        this.initSuggestions();
    }

    @Override
    public void initSuggestions() {
        this.cachedSuggestions.clear();
        for (Key key : this.byId.keySet()) {
            this.cachedSuggestions.add(Suggestion.suggestion(key.toString()));
        }
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    @Override
    public Optional<FurnitureConfig> furnitureById(Key id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    @Override
    public Map<Key, FurnitureConfig> loadedFurniture() {
        return Collections.unmodifiableMap(this.byId);
    }

    @Override
    public void unload() {
        this.byId.clear();
    }

    protected abstract FurnitureHitBoxConfig<?> defaultHitBox();

    public class FurnitureParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] { "furniture" };
        private final List<PendingConfigSection> pendingConfigSections = new ArrayList<>();

        public void addPendingConfigSection(PendingConfigSection section) {
            this.pendingConfigSections.add(section);
        }

        @Override
        public void preProcess() {
            for (PendingConfigSection section : this.pendingConfigSections) {
                ResourceConfigUtils.runCatching(
                        section.path(),
                        section.node(),
                        () -> parseSection(section.pack(), section.path(), section.node(), section.id(), section.config()),
                        () -> GsonHelper.get().toJson(section.config())
                );
            }
            this.pendingConfigSections.clear();
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.FURNITURE;
        }

        @Override
        public int count() {
            return AbstractFurnitureManager.this.byId.size();
        }

        @Override
        public void parseSection(Pack pack, Path path, String node, Key id, Map<String, Object> section) {
            if (AbstractFurnitureManager.this.byId.containsKey(id)) {
                throw new LocalizedResourceConfigException("warning.config.furniture.duplicate");
            }

            Map<String, Object> variantsMap = ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(ResourceConfigUtils.get(section, "variants", "placement", "variant"), "warning.config.furniture.missing_variants"), "variants");
            if (variantsMap.isEmpty()) {
                throw new LocalizedResourceConfigException("warning.config.furniture.missing_variants");
            }

            Map<String, FurnitureVariant> variants = new HashMap<>();
            for (Map.Entry<String, Object> e0 : variantsMap.entrySet()) {
                String variantName = e0.getKey();
                Map<String, Object> variantArguments = ResourceConfigUtils.getAsMap(e0.getValue(), variantName);
                Optional<Vector3f> optionalLootSpawnOffset = Optional.ofNullable(variantArguments.get("loot-spawn-offset")).map(it -> ResourceConfigUtils.getAsVector3f(it, "loot-spawn-offset"));
                List<FurnitureElementConfig<?>> elements = ResourceConfigUtils.parseConfigAsList(variantArguments.get("elements"), FurnitureElementConfigs::fromMap);

                // fixme 外部模型不应该在这
                Optional<ExternalModel> externalModel;
                if (variantArguments.containsKey("model-engine")) {
                    externalModel = Optional.of(plugin.compatibilityManager().createModel("ModelEngine", variantArguments.get("model-engine").toString()));
                } else if (variantArguments.containsKey("better-model")) {
                    externalModel = Optional.of(plugin.compatibilityManager().createModel("BetterModel", variantArguments.get("better-model").toString()));
                } else {
                    externalModel = Optional.empty();
                }

                List<FurnitureHitBoxConfig<?>> hitboxes = ResourceConfigUtils.parseConfigAsList(variantArguments.get("hitboxes"), FurnitureHitBoxTypes::fromMap);
                if (hitboxes.isEmpty() && externalModel.isEmpty()) {
                    hitboxes = List.of(defaultHitBox());
                }

                variants.put(variantName, new FurnitureVariant(
                    variantName,
                    parseCullingData(section.get("entity-culling")),
                    elements.toArray(new FurnitureElementConfig[0]),
                    hitboxes.toArray(new FurnitureHitBoxConfig[0]),
                    externalModel,
                    optionalLootSpawnOffset
                ));
            }

            FurnitureConfig furniture = FurnitureConfig.builder()
                    .id(id)
                    .settings(FurnitureSettings.fromMap(MiscUtils.castToMap(section.get("settings"), true)))
                    .variants(variants)
                    .events(EventFunctions.parseEvents(ResourceConfigUtils.get(section, "events", "event")))
                    .lootTable(LootTable.fromMap(MiscUtils.castToMap(section.get("loot"), true)))
                    .build();
            AbstractFurnitureManager.this.byId.put(id, furniture);
        }



        private CullingData parseCullingData(Object arguments) {
            if (arguments instanceof Boolean b && !b)
                return null;
            if (!(arguments instanceof Map))
                return new CullingData(null, Config.entityCullingViewDistance(), 0.25, true);
            Map<String, Object> argumentsMap = ResourceConfigUtils.getAsMap(arguments, "entity-culling");
            return new CullingData(
                    ResourceConfigUtils.getOrDefault(argumentsMap.get("aabb"), it -> ResourceConfigUtils.getAsAABB(it, "aabb"), null),
                    ResourceConfigUtils.getAsInt(argumentsMap.getOrDefault("view-distance", Config.entityCullingViewDistance()), "view-distance"),
                    ResourceConfigUtils.getAsDouble(argumentsMap.getOrDefault("aabb-expansion", 0.25), "aabb-expansion"),
                    ResourceConfigUtils.getAsBoolean(argumentsMap.getOrDefault("ray-tracing", true), "ray-tracing")
            );
        }
    }
}
