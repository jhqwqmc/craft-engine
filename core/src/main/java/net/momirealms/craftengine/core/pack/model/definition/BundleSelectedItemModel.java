package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;
import java.util.Map;

public final class BundleSelectedItemModel implements ItemModel {
    public static final BundleSelectedItemModel INSTANCE = new BundleSelectedItemModel();
    public static final ItemModelFactory<BundleSelectedItemModel> FACTORY = new Factory();
    public static final ItemModelReader<BundleSelectedItemModel> READER = new Reader();

    private BundleSelectedItemModel() {}

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        return List.of();
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "bundle/selected_item");
        return json;
    }

    private static class Factory implements ItemModelFactory<BundleSelectedItemModel> {
        @Override
        public BundleSelectedItemModel create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements ItemModelReader<BundleSelectedItemModel> {
        @Override
        public BundleSelectedItemModel read(JsonObject json) {
            return INSTANCE;
        }
    }
}
