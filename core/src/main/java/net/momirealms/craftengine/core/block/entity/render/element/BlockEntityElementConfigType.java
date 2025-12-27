package net.momirealms.craftengine.core.block.entity.render.element;

import net.momirealms.craftengine.core.util.Key;

public record BlockEntityElementConfigType<E extends BlockEntityElement>(Key id, BlockEntityElementConfigFactory<E> factory) {
}
