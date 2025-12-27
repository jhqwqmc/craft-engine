package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public final class BannerSpecialModel implements SpecialModel {
    public static final SpecialModelFactory FACTORY = new Factory();
    public static final SpecialModelReader READER = new Reader();
    private final String color;

    public BannerSpecialModel(String color) {
        this.color = color;
    }

    public String color() {
        return this.color;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "banner");
        json.addProperty("color", this.color);
        return json;
    }

    private static class Factory implements SpecialModelFactory {
        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            String color = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("color"), "warning.config.item.model.special.banner.missing_color");
            return new BannerSpecialModel(color);
        }
    }

    private static class Reader implements SpecialModelReader {
        @Override
        public SpecialModel read(JsonObject json) {
            String color = json.get("color").getAsString();
            return new BannerSpecialModel(color);
        }
    }
}
