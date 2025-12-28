package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public final class SignSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<SignSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<SignSpecialModel> READER = new Reader();
    private final Key type;
    private final String woodType;
    private final String texture;

    public SignSpecialModel(Key type, String woodType, String texture) {
        this.type = type;
        this.woodType = woodType;
        this.texture = texture;
    }

    public Key type() {
        return this.type;
    }

    public String woodType() {
        return this.woodType;
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
        json.addProperty("type", this.type.asMinimalString());
        json.addProperty("wood_type", woodType);
        json.addProperty("texture", texture);
        return json;
    }

    private static class Factory implements SpecialModelFactory<SignSpecialModel> {
        @Override
        public SignSpecialModel create(Map<String, Object> arguments) {
            Key type = Key.of(arguments.get("type").toString());
            String woodType = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("wood-type"), "warning.config.item.model.special.sign.missing_wood_type");
            String texture = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("texture"), "warning.config.item.model.special.sign.missing_texture");
            return new SignSpecialModel(type, woodType, texture);
        }
    }

    private static class Reader implements SpecialModelReader<SignSpecialModel> {
        @Override
        public SignSpecialModel read(JsonObject json) {
            Key type = Key.of(json.get("type").toString());
            String woodType = json.get("wood_type").getAsString();
            String texture = json.get("texture").getAsString();
            return new SignSpecialModel(type, woodType, texture);
        }
    }
}
