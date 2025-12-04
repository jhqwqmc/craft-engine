package net.momirealms.craftengine.core.entity.furniture.behavior;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.tick.FurnitureTicker;

public interface FurnitureBehavior {

    default <T extends Furniture> FurnitureTicker<T> createSyncFurnitureTicker(T furniture) {
        return null;
    }

    default <T extends Furniture> FurnitureTicker<T> createAsyncBlockEntityTicker(T furniture) {
        return null;
    }
}
