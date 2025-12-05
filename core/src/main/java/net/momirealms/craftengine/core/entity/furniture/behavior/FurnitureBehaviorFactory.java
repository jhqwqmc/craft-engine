package net.momirealms.craftengine.core.entity.furniture.behavior;

import java.util.Map;

public interface FurnitureBehaviorFactory<T extends FurnitureBehavior> {

    T create(Map<String, Object> properties);
}
