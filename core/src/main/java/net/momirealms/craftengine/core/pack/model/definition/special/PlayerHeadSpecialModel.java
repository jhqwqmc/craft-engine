package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.pack.revision.Revisions;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;
import java.util.Map;

public final class PlayerHeadSpecialModel implements SpecialModel {
    public static final Key ID = Key.of("minecraft:player_head");
    public static final SpecialModelFactory FACTORY = new Factory();
    public static final SpecialModelReader READER = new Reader();
    public static final PlayerHeadSpecialModel INSTANCE = new PlayerHeadSpecialModel();

    private PlayerHeadSpecialModel() {}

    @Override
    public List<Revision> revisions() {
        return List.of(Revisions.SINCE_1_21_6);
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        if (version.isAtOrAbove(MinecraftVersion.V1_21_6)) {
            json.addProperty("type", ID.asMinimalString());
        } else {
            json.addProperty("type", HeadSpecialModel.ID.asString());
            json.addProperty("kind", "player");
        }
        return json;
    }

    private static class Factory implements SpecialModelFactory {
        @Override
        public SpecialModel create(Map<String, Object> arguments) {
            return INSTANCE;
        }
    }

    private static class Reader implements SpecialModelReader {
        @Override
        public SpecialModel read(JsonObject json) {
            return INSTANCE;
        }
    }
}
