package net.momirealms.craftengine.core.entity.furniture.element;

import java.util.Map;

public interface FurnitureElementConfigFactory {

    <E extends FurnitureElement> FurnitureElementConfig<E> create(Map<String, Object> args);
}
