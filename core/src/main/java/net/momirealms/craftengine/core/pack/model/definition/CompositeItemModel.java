package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CompositeItemModel implements ItemModel {
    public static final Key ID = Key.of("minecraft:composite");
    public static final ItemModelFactory FACTORY = new Factory();
    public static final ItemModelReader READER = new Reader();
    private final List<ItemModel> models;

    public CompositeItemModel(List<ItemModel> models) {
        this.models = models;
    }

    @NotNull
    public List<ItemModel> models() {
        return this.models;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", ID.asMinimalString());
        JsonArray array = new JsonArray();
        for (ItemModel model : this.models) {
            array.add(model.apply(version));
        }
        json.add("models", array);
        return json;
    }

    @Override
    public List<Revision> revisions() {
        List<Revision> versions = new ArrayList<>();
        for (ItemModel model : this.models) {
            versions.addAll(model.revisions());
        }
        return versions;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        List<ModelGeneration> models = new ArrayList<>(4);
        for (ItemModel model : this.models) {
            models.addAll(model.modelsToGenerate());
        }
        return models;
    }

    private static class Factory implements ItemModelFactory {

        @SuppressWarnings("unchecked")
        @Override
        public ItemModel create(Map<String, Object> arguments) {
            Object m = arguments.get("models");
            if (m instanceof List<?> list) {
                List<Object> models = (List<Object>) list;
                if (models.isEmpty()) {
                    throw new LocalizedResourceConfigException("warning.config.item.model.composite.missing_models");
                }
                List<ItemModel> modelList = new ArrayList<>();
                for (Object model : models) {
                    modelList.add(ItemModels.fromObj(model));
                }
                return new CompositeItemModel(modelList);
            } else if (m != null) {
                return new CompositeItemModel(List.of(ItemModels.fromObj(m)));
            } else {
                throw new LocalizedResourceConfigException("warning.config.item.model.composite.missing_models");
            }
        }
    }

    private static class Reader implements ItemModelReader {

        @Override
        public ItemModel read(JsonObject json) {
            JsonArray models = json.getAsJsonArray("models");
            if (models == null) {
                throw new IllegalArgumentException("models is expected to be a JsonArray");
            }
            List<ItemModel> modelList = new ArrayList<>();
            for (JsonElement model : models) {
                if (model instanceof JsonObject jo) {
                    modelList.add(ItemModels.fromJson(jo));
                } else {
                    throw new IllegalArgumentException("model is expected to be a JsonObject");
                }
            }
            return new CompositeItemModel(modelList);
        }
    }
}
