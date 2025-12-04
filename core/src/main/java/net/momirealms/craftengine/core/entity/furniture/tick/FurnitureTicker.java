package net.momirealms.craftengine.core.entity.furniture.tick;

import net.momirealms.craftengine.core.entity.furniture.Furniture;

public interface FurnitureTicker<T extends Furniture> {

    void tick(T furniture);
}
