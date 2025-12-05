package net.momirealms.craftengine.core.entity.furniture.behavior;

import net.momirealms.craftengine.core.util.Key;

public record FurnitureBehaviorType<T extends FurnitureBehavior>(Key id, FurnitureBehaviorFactory<T> factory) {
}
