package net.momirealms.craftengine.core.entity.furniture.behavior;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class EmptyFurnitureBehavior implements FurnitureBehavior {
    private EmptyFurnitureBehavior() {}

    public static final EmptyFurnitureBehavior INSTANCE = new EmptyFurnitureBehavior();
}
