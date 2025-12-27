package net.momirealms.craftengine.core.pack.conflict.resolution;

import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class Resolutions {
    public static final ResolutionType RETAIN_MATCHING = register(Key.ce("retain_matching"), RetainMatchingResolution.FACTORY);
    public static final ResolutionType MERGE_JSON = register(Key.ce("merge_json"), MergeJsonResolution.FACTORY);
    public static final ResolutionType MERGE_ATLAS = register(Key.ce("merge_atlas"), MergeAltasResolution.FACTORY);
    public static final ResolutionType MERGE_FONT = register(Key.ce("merge_font"), MergeFontResolution.FACTORY);
    public static final ResolutionType CONDITIONAL = register(Key.ce("conditional"), ConditionalResolution.FACTORY);
    public static final ResolutionType MERGE_PACK_MCMETA = register(Key.ce("merge_pack_mcmeta"), MergePackMcMetaResolution.FACTORY);
    public static final ResolutionType MERGE_LEGACY_MODEL = register(Key.ce("merge_legacy_model"), MergeLegacyModelResolution.FACTORY);

    private Resolutions() {}

    public static ResolutionType register(Key key, ResolutionFactory factory) {
        ResolutionType type = new ResolutionType(key, factory);
        ((WritableRegistry<ResolutionType>) BuiltInRegistries.RESOLUTION_TYPE)
                .register(ResourceKey.create(Registries.RESOLUTION_TYPE.location(), key), type);
        return type;
    }

    public static Resolution fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), () -> new LocalizedException("warning.config.conflict_resolution.missing_type"));
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        ResolutionType resolutionType = BuiltInRegistries.RESOLUTION_TYPE.getValue(key);
        if (resolutionType == null) {
            throw new LocalizedException("warning.config.conflict_resolution.invalid_type", type);
        }
        return resolutionType.factory().create(map);
    }
}
