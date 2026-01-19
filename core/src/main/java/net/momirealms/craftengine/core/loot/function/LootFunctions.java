package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public final class LootFunctions {
    public static final LootFunctionType<?> APPLY_BONUS = register(Key.ce("apply_bonus"), ApplyBonusCountFunction.FACTORY);
    public static final LootFunctionType<?> APPLY_DATA = register(Key.ce("apply_data"), ApplyDataFunction.FACTORY);
    public static final LootFunctionType<?> SET_COUNT = register(Key.ce("set_count"), SetCountFunction.FACTORY);
    public static final LootFunctionType<?> EXPLOSION_DECAY = register(Key.ce("explosion_decay"), ExplosionDecayFunction.FACTORY);
    public static final LootFunctionType<?> DROP_EXP = register(Key.ce("drop_exp"), DropExpFunction.FACTORY);
    public static final LootFunctionType<?> LIMIT_COUNT = register(Key.ce("limit_count"), LimitCountFunction.FACTORY);

    private LootFunctions() {}

    public static <T> LootFunctionType<T> register(Key key, LootFunctionFactory<T> factory) {
        LootFunctionType<T> type = new LootFunctionType<>(key, factory);
        ((WritableRegistry<LootFunctionType<?>>) BuiltInRegistries.LOOT_FUNCTION_TYPE)
                .register(ResourceKey.create(Registries.LOOT_FUNCTION_TYPE.location(), key), type);
        return type;
    }

    public static <T> BiFunction<Item<T>, LootContext, Item<T>> identity() {
        return (item, context) -> item;
    }

    public static <T> BiFunction<Item<T>, LootContext, Item<T>> compose(List<? extends BiFunction<Item<T>, LootContext, Item<T>>> terms) {
        List<BiFunction<Item<T>, LootContext, Item<T>>> list = List.copyOf(terms);
        return switch (list.size()) {
            case 0 -> identity();
            case 1 -> list.get(0);
            case 2 -> {
                BiFunction<Item<T>, LootContext, Item<T>> f1 = list.get(0);
                BiFunction<Item<T>, LootContext, Item<T>> f2 = list.get(1);
                yield (item, context) -> f2.apply(f1.apply(item, context), context);
            }
            default -> (item, context) -> {
                for (BiFunction<Item<T>, LootContext, Item<T>> function : list) {
                    item = function.apply(item, context);
                }
                return item;
            };
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> LootFunction<T> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.loot_table.function.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        LootFunctionType<T> functionType = (LootFunctionType<T>) BuiltInRegistries.LOOT_FUNCTION_TYPE.getValue(key);
        if (functionType == null) {
            throw new LocalizedResourceConfigException("warning.config.loot_table.function.invalid_type", type);
        }
        return functionType.factory().create(map);
    }
}
