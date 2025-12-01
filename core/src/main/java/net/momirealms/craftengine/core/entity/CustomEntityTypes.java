package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public class CustomEntityTypes {
    public static final CustomEntityType<InactiveCustomEntity> INACTIVE = register(CustomEntityTypeKeys.INACTIVE, InactiveCustomEntity::new);

    public static <T extends CustomEntity> CustomEntityType<T> register(Key id, CustomEntityType.Factory<T> factory) {
        CustomEntityType<T> type = new CustomEntityType<>(id, factory);
        ((WritableRegistry<CustomEntityType<?>>) BuiltInRegistries.ENTITY_TYPE)
                .register(ResourceKey.create(Registries.ENTITY_TYPE.location(), id), type);
        return type;
    }
}
