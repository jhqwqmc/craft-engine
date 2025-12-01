package net.momirealms.craftengine.core.entity.furniture.element;

import net.momirealms.craftengine.core.world.WorldPosition;
import org.jetbrains.annotations.NotNull;

public interface FurnitureElementConfig<E extends FurnitureElement> {

    E create(@NotNull WorldPosition position);
}
