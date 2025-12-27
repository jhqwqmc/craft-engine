package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class SpecialModels {
    public static final SpecialModelType BANNER = register(Key.of("banner"), BannerSpecialModel.FACTORY, BannerSpecialModel.READER);
    public static final SpecialModelType BED = register(Key.of("bed"), BedSpecialModel.FACTORY, BedSpecialModel.READER);
    public static final SpecialModelType CHEST = register(Key.of("chest"), ChestSpecialModel.FACTORY, ChestSpecialModel.READER);
    public static final SpecialModelType CONDUIT = register(Key.of("conduit"), SimpleSpecialModel.FACTORY, SimpleSpecialModel.READER);
    public static final SpecialModelType COPPER_GOLEM_STATUE = register(Key.of("copper_golem_statue"), CopperGolemStatueSpecialModel.FACTORY, CopperGolemStatueSpecialModel.READER);
    public static final SpecialModelType DECORATED_POT = register(Key.of("decorated_pot"), SimpleSpecialModel.FACTORY, SimpleSpecialModel.READER);
    public static final SpecialModelType HEAD = register(Key.of("head"), HeadSpecialModel.FACTORY, HeadSpecialModel.READER);
    public static final SpecialModelType PLAYER_HEAD = register(Key.of("player_head"), PlayerHeadSpecialModel.FACTORY, PlayerHeadSpecialModel.READER);
    public static final SpecialModelType SHIELD = register(Key.of("shield"), SimpleSpecialModel.FACTORY, SimpleSpecialModel.READER);
    public static final SpecialModelType SHULKER_BOX = register(Key.of("shulker_box"), ShulkerBoxSpecialModel.FACTORY, ShulkerBoxSpecialModel.READER);
    public static final SpecialModelType STANDING_SIGN = register(Key.of("standing_sign"), SignSpecialModel.FACTORY, SignSpecialModel.READER);
    public static final SpecialModelType HANGING_SIGN = register(Key.of("hanging_sign"), SignSpecialModel.FACTORY, SignSpecialModel.READER);
    public static final SpecialModelType TRIDENT = register(Key.of("trident"), SimpleSpecialModel.FACTORY, SimpleSpecialModel.READER);

    private SpecialModels() {}

    public static SpecialModelType register(Key id, SpecialModelFactory factory, SpecialModelReader reader) {
        SpecialModelType type = new SpecialModelType(id, factory, reader);
        ((WritableRegistry<SpecialModelType>) BuiltInRegistries.SPECIAL_MODEL_TYPE)
                .register(ResourceKey.create(Registries.SPECIAL_MODEL_TYPE.location(), id), type);
        return type;
    }

    public static SpecialModel fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.item.model.special.missing_type");
        Key key = Key.withDefaultNamespace(type, "minecraft");
        SpecialModelType specialModelType = BuiltInRegistries.SPECIAL_MODEL_TYPE.getValue(key);
        if (specialModelType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.special.invalid_type", type);
        }
        return specialModelType.factory().create(map);
    }

    public static SpecialModel fromJson(JsonObject json) {
        String type = json.get("type").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        SpecialModelType specialModelType = BuiltInRegistries.SPECIAL_MODEL_TYPE.getValue(key);
        if (specialModelType == null) {
            throw new IllegalArgumentException("Invalid special model type: " + key);
        }
        return specialModelType.reader().read(json);
    }
}
