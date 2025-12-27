package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.entry.LootEntryContainer;
import net.momirealms.craftengine.core.loot.entry.LootEntryContainers;
import net.momirealms.craftengine.core.loot.function.LootFunction;
import net.momirealms.craftengine.core.loot.function.LootFunctions;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LootTable<T> {
    private final List<LootPool<T>> pools;
    private final List<LootFunction<T>> functions;
    private final BiFunction<Item<T>, LootContext, Item<T>> compositeFunction;

    public LootTable(List<LootPool<T>> pools, List<LootFunction<T>> functions) {
        this.pools = pools;
        this.functions = functions;
        this.compositeFunction = LootFunctions.compose(functions);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> LootTable<T> fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;
        Object pools = ResourceConfigUtils.requireNonNullOrThrow(map.get("pools"), "warning.config.loot_table.missing_pools");
        if (!(pools instanceof List<?> list) || list.isEmpty()) {
            throw new LocalizedResourceConfigException("warning.config.loot_table.invalid_pools_type", pools.getClass().getSimpleName());
        }
        List<Object> poolList = (List<Object>) map.get("pools");
        List<LootPool<T>> lootPools = new ArrayList<>();
        for (Object rawPool : poolList) {
            if (rawPool instanceof Map<?,?> rawPoolMap) {
                Map<String, Object> pool = MiscUtils.castToMap(rawPoolMap, false);
                NumberProvider rolls = NumberProviders.fromObject(pool.getOrDefault("rolls", 1));
                NumberProvider bonus_rolls = NumberProviders.fromObject(pool.getOrDefault("bonus_rolls", 0));
                List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(pool.get("conditions"), CommonConditions::fromMap);
                List<LootEntryContainer<T>> containers = ResourceConfigUtils.parseConfigAsList(pool.get("entries"), LootEntryContainers::fromMap);
                List<LootFunction<T>> functions = ResourceConfigUtils.parseConfigAsList(pool.get("functions"), LootFunctions::fromMap);
                lootPools.add(new LootPool<>(containers, conditions, functions, rolls, bonus_rolls));
            } else if (rawPool instanceof String string) {
                LootPool<T> lootPool = readFlatFormatLootPool(string);
                if (lootPool != null)
                    lootPools.add(lootPool);
            }
        }
        return new LootTable<>(lootPools, ResourceConfigUtils.parseConfigAsList(map.get("functions"), LootFunctions::fromMap));
    }

    public List<Item<T>> getRandomItems(ContextHolder parameters, World world) {
        return this.getRandomItems(parameters, world, null);
    }

    public List<Item<T>> getRandomItems(ContextHolder parameters, World world, @Nullable Player player) {
        return this.getRandomItems(new LootContext(world, player, player == null ? 1f : (float) player.luck(), parameters));
    }

    private List<Item<T>> getRandomItems(LootContext context) {
        ArrayList<Item<T>> list = new ArrayList<>();
        this.getRandomItems(context, list::add);
        return list;
    }

    public void getRandomItems(LootContext context, Consumer<Item<T>> lootConsumer) {
        this.getRandomItemsRaw(context, createFunctionApplier(createStackSplitter(lootConsumer), context));
    }

    private Consumer<Item<T>> createFunctionApplier(Consumer<Item<T>> lootConsumer, LootContext context) {
        return (item -> {
            for (LootFunction<T> function : this.functions) {
                function.apply(item, context);
            }
            lootConsumer.accept(item);
        });
    }

    private Consumer<Item<T>> createStackSplitter(Consumer<Item<T>> consumer) {
        return (item) -> {
            if (item.count() < item.maxStackSize()) {
                consumer.accept(item);
            } else {
                int remaining = item.count();
                while (remaining > 0) {
                    Item<T> splitItem = item.copyWithCount(Math.min(item.maxStackSize(), remaining));
                    remaining -= splitItem.count();
                    consumer.accept(splitItem);
                }
            }
        };
    }

    public void getRandomItemsRaw(LootContext context, Consumer<Item<T>> lootConsumer) {
        Consumer<Item<T>> consumer = LootFunction.decorate(this.compositeFunction, lootConsumer, context);
        for (LootPool<T> pool : this.pools) {
            pool.addRandomItems(consumer, context);
        }
    }

    public static <T> LootPool<T> readFlatFormatLootPool(String pool) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castToMapListOrThrow(Object obj, Supplier<RuntimeException> exceptionSupplier) {
        if (obj instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        } else {
            throw exceptionSupplier.get();
        }
    }
}
