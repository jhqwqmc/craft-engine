package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public final class BedSpecialModel implements SpecialModel {
    public static final SpecialModelFactory FACTORY = new Factory();
    public static final SpecialModelReader READER = new Reader();
    private final String texture;

    public BedSpecialModel(String texture) {
        this.texture = texture;
    }

    public String texture() {
        return this.texture;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "bed");
        json.addProperty("texture", this.texture);
        return json;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    private static class Factory implements SpecialModelFactory {
        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            String texture = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("texture"), "warning.config.item.model.special.bed.missing_texture");
            return new BedSpecialModel(texture);
        }
    }

    private static class Reader implements SpecialModelReader {
        @Override
        public SpecialModel read(JsonObject json) {
            String texture = json.get("texture").getAsString();
            return new BedSpecialModel(texture);
        }
    }
}
