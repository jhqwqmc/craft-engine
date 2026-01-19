package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;
import java.util.Map;

public final class EmptyItemModel implements ItemModel {
    public static final ItemModelFactory<EmptyItemModel> FACTORY = new Factory();
    public static final ItemModelReader<EmptyItemModel> READER = new Reader();
    private static final EmptyItemModel INSTANCE = new EmptyItemModel();

    private EmptyItemModel() {}

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "empty");
        return json;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        return List.of();
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    private static class Factory implements ItemModelFactory<EmptyItemModel> {
        @Override
        public EmptyItemModel create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements ItemModelReader<EmptyItemModel> {
        @Override
        public EmptyItemModel read(JsonObject json) {
            return INSTANCE;
        }
    }
}
