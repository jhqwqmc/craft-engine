package net.momirealms.craftengine.core.entity.furniture.behavior;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@ApiStatus.Experimental
public interface FurnitureBehaviorFactory<T extends FurnitureBehavior> {

    T create(Map<String, Object> properties);
}
