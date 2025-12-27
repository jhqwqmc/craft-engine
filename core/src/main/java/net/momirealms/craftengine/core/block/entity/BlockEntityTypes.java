package net.momirealms.craftengine.core.block.entity;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public abstract class BlockEntityTypes {
    public static final BlockEntityType<InactiveBlockEntity> INACTIVE = register(BlockEntityTypeKeys.INACTIVE);

    public static <T extends BlockEntity> BlockEntityType<T> register(Key id) {
        BlockEntityType<T> type = new BlockEntityType<>(id);
        ((WritableRegistry<BlockEntityType<? extends BlockEntity>>) BuiltInRegistries.BLOCK_ENTITY_TYPE)
                .register(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE.location(), id), type);
        return type;
    }
}
