package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BlockBehaviors {
    public static final BlockBehaviorType EMPTY = register(Key.ce("empty"), (block, args) -> EmptyBlockBehavior.INSTANCE);

    protected BlockBehaviors() {
    }

    public static BlockBehaviorType register(Key key, BlockBehaviorFactory factory) {
        BlockBehaviorType type = new BlockBehaviorType(key, factory);
        ((WritableRegistry<BlockBehaviorType>) BuiltInRegistries.BLOCK_BEHAVIOR_TYPE)
                .register(ResourceKey.create(Registries.BLOCK_BEHAVIOR_TYPE.location(), key), type);
        return type;
    }

    public static BlockBehavior fromMap(CustomBlock block, @Nullable Map<String, Object> map) {
        if (map == null || map.isEmpty()) return EmptyBlockBehavior.INSTANCE;
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.block.behavior.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        BlockBehaviorType factory = BuiltInRegistries.BLOCK_BEHAVIOR_TYPE.getValue(key);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.block.behavior.invalid_type", type);
        }
        return factory.factory().create(block, map);
    }
}
