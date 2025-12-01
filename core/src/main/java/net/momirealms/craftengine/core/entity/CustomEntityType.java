package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.UUID;

public record CustomEntityType<T extends CustomEntity>(Key id, Factory<T> factory) {

    public interface Factory<T extends CustomEntity> {

        T create(UUID uuid, WorldPosition position);
    }
}
