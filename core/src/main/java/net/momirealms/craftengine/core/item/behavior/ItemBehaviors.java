package net.momirealms.craftengine.core.item.behavior;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.nio.file.Path;
import java.util.Map;

public class ItemBehaviors {
    public static final ItemBehaviorType<EmptyItemBehavior> EMPTY = register(Key.withDefaultNamespace("empty", Key.DEFAULT_NAMESPACE), EmptyItemBehavior.FACTORY);

    public static <T extends ItemBehavior> ItemBehaviorType<T> register(Key key, ItemBehaviorFactory<T> factory) {
        ItemBehaviorType<T> type = new ItemBehaviorType<>(key, factory);
        ((WritableRegistry<ItemBehaviorType<? extends ItemBehavior>>) BuiltInRegistries.ITEM_BEHAVIOR_TYPE)
                .register(ResourceKey.create(Registries.ITEM_BEHAVIOR_TYPE.location(), key), type);
        return type;
    }

    public static ItemBehavior fromMap(Pack pack, Path path, String node, Key id, Map<String, Object> map) {
        if (map == null || map.isEmpty()) return EmptyItemBehavior.INSTANCE;
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.item.behavior.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        ItemBehaviorType<? extends ItemBehavior> behaviorType = BuiltInRegistries.ITEM_BEHAVIOR_TYPE.getValue(key);
        if (behaviorType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.behavior.invalid_type", type);
        }
        return behaviorType.factory().create(pack, path, node, id, map);
    }
}