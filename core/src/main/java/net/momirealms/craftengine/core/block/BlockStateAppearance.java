package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.Optional;

public record BlockStateAppearance(BlockStateWrapper blockState,
                                   Optional<BlockEntityElementConfig<? extends BlockEntityElement>[]> blockEntityRenderer,
                                   AABB estimateAABB) {
}
