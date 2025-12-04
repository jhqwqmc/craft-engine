package net.momirealms.craftengine.core.block.entity.render.element;

import java.util.Map;

@FunctionalInterface
public interface BlockEntityElementConfigFactory<E extends BlockEntityElement> {

    BlockEntityElementConfig<E> create(Map<String, Object> args);
}
