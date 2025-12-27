package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;
import java.util.Optional;

public class FurnitureHitBoxes {
    protected FurnitureHitBoxes() {}

    public static <H extends FurnitureHitBox> FurnitureHitboxConfigType<H> register(Key key, FurnitureHitBoxConfigFactory<H> factory) {
        FurnitureHitboxConfigType<H> type = new FurnitureHitboxConfigType<>(key, factory);
        ((WritableRegistry<FurnitureHitboxConfigType<? extends FurnitureHitBox>>) BuiltInRegistries.FURNITURE_HITBOX_TYPE)
                .register(ResourceKey.create(Registries.FURNITURE_HITBOX_TYPE.location(), key), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <H extends FurnitureHitBox> FurnitureHitBoxConfig<H> fromMap(Map<String, Object> arguments) {
        String typeString = Optional.ofNullable(arguments.get("type")).map(String::valueOf).orElse("interaction");
        Key type = Key.ce(typeString);
        FurnitureHitboxConfigType<H> configType = (FurnitureHitboxConfigType<H>) BuiltInRegistries.FURNITURE_HITBOX_TYPE.getValue(type);
        if (configType == null) {
            throw new LocalizedResourceConfigException("warning.config.furniture.hitbox.invalid_type", typeString);
        }
        return configType.factory().create(arguments);
    }
}