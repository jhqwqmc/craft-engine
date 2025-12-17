package net.momirealms.craftengine.core.pack.conflict.resolution;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class ResolutionMergePackMcMeta implements Resolution {
    public static final Factory FACTORY = new Factory();
    public static final Set<String> STANDARD_PACK_KEYS = ImmutableSet.of("pack", "features", "filter", "overlays", "language");
    public static final PackVersion MIN_PACK_VERSION = new PackVersion(15, 0); // 1.20
    public static final PackVersion MAX_PACK_VERSION = new PackVersion(1000, 0); // future
    private final String description;

    public ResolutionMergePackMcMeta(String description) {
        this.description = description;
    }

    public static void merge(Path file1, Path file2, JsonElement customDescription) throws IOException {
        // 第一步，解析全部的mcmeta文件为json对象
        JsonObject mcmeta1;
        try {
            mcmeta1 = GsonHelper.readJsonFile(file1).getAsJsonObject();
        } catch (Exception e) {
            CraftEngine.instance().logger().severe("Failed to parse mcmeta from " + file1);
            return;
        }
        JsonObject mcmeta2;
        try {
            mcmeta2 = GsonHelper.readJsonFile(file2).getAsJsonObject();
        } catch (Exception e) {
            CraftEngine.instance().logger().severe("Failed to parse mcmeta from " + file2);
            return;
        }
        JsonObject merged = new JsonObject();
        // 第三步，处理pack区域
        JsonObject pack1 = mcmeta1.getAsJsonObject("pack");
        JsonObject pack2 = mcmeta2.getAsJsonObject("pack");
        JsonObject mergedPack = new JsonObject();
        mergedPack.add("description", customDescription);
        merged.add("pack", mergedPack);
        mergePack(mergedPack, pack1, pack2);
        // 第三步，合并overlays
        List<JsonObject> overlays = new ArrayList<>();
        collectOverlays(mcmeta1.getAsJsonObject("overlays"), overlays::add);
        collectOverlays(mcmeta2.getAsJsonObject("overlays"), overlays::add);
        if (!overlays.isEmpty()) {
            Map<String, JsonObject> overlayMap = new LinkedHashMap<>();
            for (JsonObject overlay : overlays) {
                JsonPrimitive directory = overlay.getAsJsonPrimitive("directory");
                if (directory != null) {
                    // 名字相同的大概率内部版本也一致，不进一步处理了
                    overlayMap.put(directory.getAsString(), overlay);
                }
            }
            if (!overlayMap.isEmpty()) {
                JsonObject mergedOverlay = new JsonObject();
                JsonArray entries = new JsonArray();
                for (JsonObject entry : overlayMap.values()) {
                    entries.add(entry);
                }
                mergedOverlay.add("entries", entries);
                merged.add("overlays", mergedOverlay);
            }
        }
        // 第四步，合并filter
        List<JsonObject> filters = new ArrayList<>();
        collectFilters(mcmeta1.getAsJsonObject("filter"), filters::add);
        collectFilters(mcmeta2.getAsJsonObject("filter"), filters::add);
        if (!filters.isEmpty()) {
            JsonObject mergedFilter = new JsonObject();
            JsonArray blocks = new JsonArray();
            for (JsonObject entry : filters) {
                blocks.add(entry);
            }
            mergedFilter.add("block", blocks);
            merged.add("filter", mergedFilter);
        }
        // 第五步，合并features
        JsonArray enabledFeatures = new JsonArray();
        getOptionalFeatures(mcmeta1.getAsJsonObject("features")).ifPresent(enabledFeatures::addAll);
        getOptionalFeatures(mcmeta2.getAsJsonObject("features")).ifPresent(enabledFeatures::addAll);
        if (!enabledFeatures.isEmpty()) {
            JsonObject features = new JsonObject();
            features.add("enabled", enabledFeatures);
            merged.add("features", features);
        }
        // 第六步，合并language
        JsonObject newLanguage = new JsonObject();
        getOptionalLanguage(mcmeta1.getAsJsonObject("language")).ifPresent(it -> {
            for (Map.Entry<String, JsonElement> entry : it.entrySet()) {
                newLanguage.add(entry.getKey(), entry.getValue());
            }
        });
        getOptionalLanguage(mcmeta2.getAsJsonObject("language")).ifPresent(it -> {
            for (Map.Entry<String, JsonElement> entry : it.entrySet()) {
                newLanguage.add(entry.getKey(), entry.getValue());
            }
        });
        if (!newLanguage.asMap().isEmpty()) { // 兼容低版本gson
            merged.add("language", newLanguage);
        }
        // 第七步，合并其他未知元素
        for (Map.Entry<String, JsonElement> entry : mcmeta1.entrySet()) {
            if (!STANDARD_PACK_KEYS.contains(entry.getKey())) {
                merged.add(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, JsonElement> entry : mcmeta2.entrySet()) {
            if (!STANDARD_PACK_KEYS.contains(entry.getKey())) {
                merged.add(entry.getKey(), entry.getValue());
            }
        }
        // 第八步，写入
        GsonHelper.writeJsonFile(merged, file1);
    }

    private static Optional<JsonObject> getOptionalLanguage(JsonObject language) {
        if (language == null) return Optional.empty();
        return Optional.of(language);
    }

    private static Optional<JsonArray> getOptionalFeatures(JsonObject feature) {
        if (feature == null) return Optional.empty();
        return Optional.ofNullable(feature.getAsJsonArray("enabled"));
    }

    private static void collectFilters(JsonObject filterJson, Consumer<JsonObject> overlayCollector) {
        if (filterJson == null) return;
        JsonArray entries = filterJson.getAsJsonArray("block");
        if (entries == null) return;
        for (JsonElement entry : entries) {
            if (entry.isJsonObject()) {
                JsonObject entryJson = entry.getAsJsonObject();
                if (entryJson == null) continue;
                overlayCollector.accept(entryJson);
            }
        }
    }

    private static void collectOverlays(JsonObject overlayJson, Consumer<JsonObject> overlayCollector) {
        if (overlayJson == null) return;
        JsonArray entries = overlayJson.getAsJsonArray("entries");
        if (entries == null) return;
        for (JsonElement entry : entries) {
            if (entry.isJsonObject()) {
                JsonObject entryJson = entry.getAsJsonObject();
                if (entryJson == null) continue;
                Pair<PackVersion, PackVersion> supportedVersions = getSupportedVersions(entryJson);
                PackVersion min = supportedVersions.left();
                PackVersion max = supportedVersions.right();
                // 旧版格式支持
                JsonObject supportedFormats = new JsonObject();
                supportedFormats.addProperty("min_inclusive", min.major);
                supportedFormats.addProperty("max_inclusive", max.major);
                entryJson.add("formats", supportedFormats);
                // 新版格式支持
                JsonArray minFormat = new JsonArray();
                minFormat.add(min.major);
                minFormat.add(min.minor);
                entryJson.add("min_format", minFormat);
                JsonArray maxFormat = new JsonArray();
                maxFormat.add(max.major);
                maxFormat.add(max.minor);
                entryJson.add("max_format", maxFormat);
                overlayCollector.accept(entryJson);
            }
        }
    }

    public record PackVersion(int major, int minor) implements Comparable<PackVersion> {

        @Override
        public int compareTo(@NotNull ResolutionMergePackMcMeta.PackVersion o) {
            // 首先比较 major 版本
            int majorCompare = Integer.compare(this.major, o.major);
            if (majorCompare != 0) {
                return majorCompare;
            }
            // 如果 major 相同，则比较 minor 版本
            return Integer.compare(this.minor, o.minor);
        }

        /**
         * 返回两个版本中较小的那个（版本较低的）
         */
        public static PackVersion getLower(PackVersion v1, PackVersion v2) {
            if (v1 == null) return v2;
            if (v2 == null) return v1;
            return v1.compareTo(v2) <= 0 ? v1 : v2;
        }

        /**
         * 返回两个版本中较大的那个（版本较高的）
         */
        public static PackVersion getHigher(PackVersion v1, PackVersion v2) {
            if (v1 == null) return v2;
            if (v2 == null) return v1;
            return v1.compareTo(v2) >= 0 ? v1 : v2;
        }

        public static PackVersion getLowest(List<PackVersion> versions) {
            if (versions == null || versions.isEmpty()) {
                return MIN_PACK_VERSION;
            }

            PackVersion lowest = versions.getFirst();
            for (int i = 1; i < versions.size(); i++) {
                lowest = getLower(lowest, versions.get(i));
            }
            return lowest;
        }

        public static PackVersion getHighest(List<PackVersion> versions) {
            if (versions == null || versions.isEmpty()) {
                return MAX_PACK_VERSION;
            }

            PackVersion highest = versions.getFirst();
            for (int i = 1; i < versions.size(); i++) {
                highest = getHigher(highest, versions.get(i));
            }
            return highest;
        }

        public static PackVersion parse(float num) {
            String str = String.valueOf(num);
            String[] parts = str.split("\\.");
            int integerPart = Integer.parseInt(parts[0]);
            int decimalPart = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return new PackVersion(integerPart, decimalPart);
        }
    }

    public static void mergePack(JsonObject merged, JsonObject pack1, JsonObject pack2) {
        Pair<PackVersion, PackVersion> pack1Version = getSupportedVersions(pack1);
        Pair<PackVersion, PackVersion> pack2Version = getSupportedVersions(pack2);
        PackVersion min = PackVersion.getLower(pack1Version.left(), pack2Version.left());
        PackVersion max = PackVersion.getHigher(pack1Version.right(), pack2Version.right());
        // 旧版格式支持
        JsonObject supportedFormats = new JsonObject();
        supportedFormats.addProperty("min_inclusive", min.major);
        supportedFormats.addProperty("max_inclusive", max.major);
        merged.add("supported_formats", supportedFormats);
        merged.addProperty("pack_format", min.major);
        // 新版格式支持
        JsonArray minFormat = new JsonArray();
        minFormat.add(min.major);
        minFormat.add(min.minor);
        merged.add("min_format", minFormat);
        JsonArray maxFormat = new JsonArray();
        maxFormat.add(max.major);
        maxFormat.add(max.minor);
        merged.add("max_format", maxFormat);
    }

    private static Pair<PackVersion, PackVersion> getSupportedVersions(JsonObject pack) {
        if (pack == null) return Pair.of(MIN_PACK_VERSION, MAX_PACK_VERSION);
        List<PackVersion> minVersions = new ArrayList<>();
        List<PackVersion> maxVersions = new ArrayList<>();
        if (pack.has("pack_format")) {
            minVersions.add(new PackVersion(pack.get("pack_format").getAsInt(), 0));
        }
        if (pack.has("min_format")) {
            minVersions.add(getFormatVersion(pack.get("min_format"), MIN_PACK_VERSION));
        }
        if (pack.has("max_format")) {
            maxVersions.add(getFormatVersion(pack.get("max_format"), MAX_PACK_VERSION));
        }
        if (pack.has("supported_formats")) {
            Pair<PackVersion, PackVersion> supportedFormats = parseSupportedFormats(pack.get("supported_formats"));
            minVersions.add(supportedFormats.left());
            maxVersions.add(supportedFormats.right());
        }
        if (pack.has("formats")) {
            Pair<PackVersion, PackVersion> supportedFormats = parseSupportedFormats(pack.get("formats"));
            minVersions.add(supportedFormats.left());
            maxVersions.add(supportedFormats.right());
        }
        return Pair.of(
                PackVersion.getLowest(minVersions),
                PackVersion.getHighest(maxVersions)
        );
    }

    private static Pair<PackVersion, PackVersion> parseSupportedFormats(JsonElement formats) {
        switch (formats) {
            case null -> {
                return Pair.of(MIN_PACK_VERSION, MAX_PACK_VERSION);
            }
            case JsonPrimitive jsonPrimitive -> {
                return new Pair<>(new PackVersion(jsonPrimitive.getAsInt(), 0), new PackVersion(jsonPrimitive.getAsInt(), 0));
            }
            case JsonArray array -> {
                if (array.isEmpty()) return Pair.of(MIN_PACK_VERSION, MAX_PACK_VERSION);
                if (array.size() == 1) {
                    return new Pair<>(new PackVersion(GsonHelper.getAsInt(array.get(0), MIN_PACK_VERSION.major), 0), MAX_PACK_VERSION);
                }
                if (array.size() == 2) {
                    return new Pair<>(new PackVersion(GsonHelper.getAsInt(array.get(0), MIN_PACK_VERSION.major), 0), new PackVersion(GsonHelper.getAsInt(array.get(1), MAX_PACK_VERSION.major), 0));
                }
            }
            case JsonObject object -> {
                int min = GsonHelper.getAsInt(object.get("min_inclusive"), MIN_PACK_VERSION.major);
                int max = GsonHelper.getAsInt(object.get("max_inclusive"), MAX_PACK_VERSION.major);
                return new Pair<>(new PackVersion(min, 0), new PackVersion(max, 0));
            }
            default -> {
            }
        }
        return Pair.of(MIN_PACK_VERSION, MAX_PACK_VERSION);
    }

    private static PackVersion getFormatVersion(JsonElement format, PackVersion defaultVersion) {
        if (format instanceof JsonArray array) {
            if (array.isEmpty()) return defaultVersion;
            if (array.size() == 1) {
                return new PackVersion(GsonHelper.getAsInt(array.get(0), defaultVersion.major), 0);
            }
            if (array.size() == 2) {
                return new PackVersion(GsonHelper.getAsInt(array.get(0), defaultVersion.major), GsonHelper.getAsInt(array.get(1), defaultVersion.minor));
            }
        } else if (format instanceof JsonPrimitive jsonPrimitive) {
            float version = jsonPrimitive.getAsFloat();
            return PackVersion.parse(version);
        }
        return defaultVersion;
    }

    @Override
    public void run(PathContext existing, PathContext conflict) {
        try {
            merge(existing.path(), conflict.path(), AdventureHelper.componentToJsonElement(AdventureHelper.miniMessage().deserialize(this.description)));
        } catch (Exception e) {
            CraftEngine.instance().logger().severe("Failed to merge pack.mcmeta when resolving file conflicts for '" + existing.path()  + "' and '" + conflict.path() + "'", e);
        }
    }

    @Override
    public Key type() {
        return Resolutions.MERGE_PACK_MCMETA;
    }

    public static class Factory implements ResolutionFactory {
        @Override
        public Resolution create(Map<String, Object> arguments) {
            String description = arguments.getOrDefault("description", "<gray>CraftEngine ResourcePack</gray>").toString();
            return new ResolutionMergePackMcMeta(description);
        }
    }
}
