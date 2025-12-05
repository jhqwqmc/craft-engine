package net.momirealms.craftengine.core.entity.furniture.tick;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class TickingFurnitureImpl<T extends Furniture> implements TickingFurniture {
    private final T furniture;
    private final FurnitureTicker<T> ticker;

    public TickingFurnitureImpl(T furniture, FurnitureTicker<T> ticker) {
        this.furniture = furniture;
        this.ticker = ticker;
    }

    @Override
    public void tick() {
        this.ticker.tick(this.furniture);
    }

    @Override
    public boolean isValid() {
        return this.furniture.isValid();
    }

    @Override
    public int entityId() {
        return this.furniture.entityId();
    }
}
