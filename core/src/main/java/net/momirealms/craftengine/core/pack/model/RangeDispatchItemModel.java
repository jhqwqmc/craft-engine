package net.momirealms.craftengine.core.pack.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.model.rangedisptach.RangeDispatchProperties;
import net.momirealms.craftengine.core.pack.model.rangedisptach.RangeDispatchProperty;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangeDispatchItemModel implements ItemModel {
    public static final Factory FACTORY = new Factory();
    private final RangeDispatchProperty property;
    private final float scale;
    private final ItemModel fallBack;
    private final Map<Float, ItemModel> entries;

    public RangeDispatchItemModel(@NotNull RangeDispatchProperty property,
                                  float scale,
                                  @Nullable ItemModel fallBack,
                                  @NotNull Map<Float, ItemModel> entries) {
        this.property = property;
        this.scale = scale;
        this.fallBack = fallBack;
        this.entries = entries;
    }

    public RangeDispatchProperty property() {
        return property;
    }

    public float scale() {
        return scale;
    }

    @Nullable
    public ItemModel fallBack() {
        return fallBack;
    }

    public Map<Float, ItemModel> entries() {
        return entries;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type().toString());
        property.accept(json);
        JsonArray array = new JsonArray();
        for (Map.Entry<Float, ItemModel> entry : entries.entrySet()) {
            float threshold = entry.getKey();
            ItemModel model = entry.getValue();
            JsonObject jo = new JsonObject();
            jo.addProperty("threshold", threshold);
            jo.add("model", model.get());
            array.add(jo);
        }
        json.add("entries", array);
        if (scale != 1) {
            json.addProperty("scale", scale);
        }
        if (fallBack != null) {
            json.add("fallback", fallBack.get());
        }
        return json;
    }

    @Override
    public Key type() {
        return ItemModels.RANGE_DISPATCH;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        List<ModelGeneration> models = new ArrayList<>(4);
        if (fallBack != null) {
            models.addAll(fallBack.modelsToGenerate());
        }
        for (ItemModel model : entries.values()) {
            models.addAll(model.modelsToGenerate());
        }
        return models;
    }

    public static class Factory implements ItemModelFactory {

        @SuppressWarnings("unchecked")
        @Override
        public ItemModel create(Map<String, Object> arguments) {
            RangeDispatchProperty property = RangeDispatchProperties.fromMap(arguments);
            float scale = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("scale", 1.0), "scale");
            Map<String, Object> fallback = MiscUtils.castToMap(arguments.get("fallback"), true);
            Object entriesObj = arguments.get("entries");
            if (entriesObj instanceof List<?> list) {
                List<Map<String, Object>> entries = (List<Map<String, Object>>) list;
                if (!entries.isEmpty()) {
                    Map<Float, ItemModel> entryMap = new HashMap<>();
                    for (Map<String, Object> entry : entries) {
                        float threshold = ResourceConfigUtils.getAsFloat(entry.getOrDefault("threshold", 1), "threshold");
                        Object model = entry.getOrDefault("model", fallback);
                        if (model == null) {
                            throw new LocalizedResourceConfigException("warning.config.item.model.range_dispatch.entry.missing_model");
                        }
                        entryMap.put(threshold, ItemModels.fromMap(MiscUtils.castToMap(model, false)));
                    }
                    return new RangeDispatchItemModel(property, scale, fallback == null ? null : ItemModels.fromMap(fallback), entryMap);
                } else {
                    throw new LocalizedResourceConfigException("warning.config.item.model.range_dispatch.missing_entries");
                }
            } else {
                throw new LocalizedResourceConfigException("warning.config.item.model.range_dispatch.missing_entries");
            }
        }
    }
}
