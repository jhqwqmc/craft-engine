package net.momirealms.craftengine.core.pack.model.legacy;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LegacyItemModel {
    private final List<ModelGeneration> modelsToGenerate;
    private final String path;
    private final List<LegacyOverridesModel> overrides;

    public LegacyItemModel(String path, List<LegacyOverridesModel> overrides, List<ModelGeneration> modelsToGenerate) {
        this.modelsToGenerate = modelsToGenerate;
        this.path = path;
        this.overrides = overrides;
    }

    public List<ModelGeneration> modelsToGenerate() {
        return modelsToGenerate;
    }

    public List<LegacyOverridesModel> overrides() {
        return overrides;
    }

    public String path() {
        return path;
    }

    @SuppressWarnings("unchecked")
    public static LegacyItemModel fromMap(Map<String, Object> legacyModel, int customModelData) {
        String legacyModelPath = ResourceConfigUtils.requireNonEmptyStringOrThrow(legacyModel.get("path"), "warning.config.item.legacy_model.missing_path");
        Map<String, Object> generation = MiscUtils.castToMap(legacyModel.get("generation"), true);
        ModelGeneration baseModelGeneration = null;
        if (generation != null) {
            baseModelGeneration = ModelGeneration.of(Key.of(legacyModelPath), generation);
        }
        List<Map<String, Object>> overrides = (List<Map<String, Object>>) legacyModel.get("overrides");
        if (overrides != null) {
            List<ModelGeneration> modelGenerations = new ArrayList<>();
            List<LegacyOverridesModel> legacyOverridesModels = new ArrayList<>();
            if (baseModelGeneration != null) modelGenerations.add(baseModelGeneration);
            legacyOverridesModels.add(new LegacyOverridesModel(new HashMap<>(), legacyModelPath, customModelData));
            for (Map<String, Object> override : overrides) {
                String overrideModelPath = ResourceConfigUtils.requireNonEmptyStringOrThrow(override.get("path"), () -> new LocalizedResourceConfigException("warning.config.item.legacy_model.overrides.missing_path"));
                Map<String, Object> predicate = MiscUtils.castToMap(ResourceConfigUtils.requireNonNullOrThrow(override.get("predicate"), "warning.config.item.legacy_model.overrides.missing_predicate"), false);
                if (predicate.isEmpty()) {
                    throw new LocalizedResourceConfigException("warning.config.item.legacy_model.overrides.missing_predicate");
                }
                Map<String, Object> overrideGeneration = MiscUtils.castToMap(override.get("generation"), true);
                if (overrideGeneration != null) {
                    modelGenerations.add(ModelGeneration.of(Key.of(overrideModelPath), overrideGeneration));
                }
                legacyOverridesModels.add(new LegacyOverridesModel(predicate, overrideModelPath, customModelData));
            }
            return new LegacyItemModel(legacyModelPath, legacyOverridesModels, modelGenerations);
        } else {
            return new LegacyItemModel(legacyModelPath,
                    List.of(new LegacyOverridesModel(new HashMap<>(), legacyModelPath, customModelData)),
                    baseModelGeneration == null ? List.of() : List.of(baseModelGeneration)
            );
        }
    }
}
