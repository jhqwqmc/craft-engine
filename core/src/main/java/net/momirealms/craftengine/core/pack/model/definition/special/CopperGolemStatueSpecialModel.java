package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public final class CopperGolemStatueSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<CopperGolemStatueSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<CopperGolemStatueSpecialModel> READER = new Reader();
    private final String pose;
    private final String texture;

    public CopperGolemStatueSpecialModel(String pose, String texture) {
        this.pose = pose;
        this.texture = texture;
    }

    public String pose() {
        return this.pose;
    }

    public String texture() {
        return this.texture;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "copper_golem_statue");
        json.addProperty("pose", this.pose);
        json.addProperty("texture", this.texture);
        return json;
    }

    private static class Factory implements SpecialModelFactory<CopperGolemStatueSpecialModel> {
        @Override
        public CopperGolemStatueSpecialModel create(Map<String, Object> arguments) {
            String pose = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("pose"), "warning.config.item.model.special.copper_golem_statue.missing_pose");
            String texture = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("texture"), "warning.config.item.model.special.copper_golem_statue.missing_texture");
            return new CopperGolemStatueSpecialModel(pose, texture);
        }
    }

    private static class Reader implements SpecialModelReader<CopperGolemStatueSpecialModel> {
        @Override
        public CopperGolemStatueSpecialModel read(JsonObject json) {
            String pose = json.get("pose").getAsString();
            String texture = json.get("texture").getAsString();
            return new CopperGolemStatueSpecialModel(pose, texture);
        }
    }
}
