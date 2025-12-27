package net.momirealms.craftengine.core.block.entity.render.element;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;
import java.util.Optional;

public abstract class BlockEntityElementConfigs {

    protected BlockEntityElementConfigs() {}

    public static <E extends BlockEntityElement> BlockEntityElementConfigType<E> register(Key key, BlockEntityElementConfigFactory<E> factory) {
        BlockEntityElementConfigType<E> type = new BlockEntityElementConfigType<>(key, factory);
        ((WritableRegistry<BlockEntityElementConfigType<?>>) BuiltInRegistries.BLOCK_ENTITY_ELEMENT_TYPE)
                .register(ResourceKey.create(Registries.BLOCK_ENTITY_ELEMENT_TYPE.location(), key), type);
        return type;
    }

    public static <E extends BlockEntityElement> BlockEntityElementConfig<E> fromMap(Map<String, Object> arguments) {
        Key type = guessType(arguments);
        @SuppressWarnings("unchecked")
        BlockEntityElementConfigType<E> configType = (BlockEntityElementConfigType<E>) BuiltInRegistries.BLOCK_ENTITY_ELEMENT_TYPE.getValue(type);
        if (configType == null) {
            throw new LocalizedResourceConfigException("warning.config.block.state.entity_renderer.invalid_type", type.toString());
        }
        return configType.factory().create(arguments);
    }

    private static Key guessType(Map<String, Object> arguments) {
        return Key.ce(Optional.ofNullable(arguments.get("type")).map(String::valueOf).orElseGet(() -> {
            if (arguments.containsKey("text")) {
                return "text_display";
            } else {
                return "item_display";
            }
        }));
    }
}
