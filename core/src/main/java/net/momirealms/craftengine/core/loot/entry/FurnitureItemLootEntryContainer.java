package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.function.LootFunction;
import net.momirealms.craftengine.core.loot.function.LootFunctions;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class FurnitureItemLootEntryContainer<T> extends SingleItemLootEntryContainer<T> {
    public static final LootEntryContainerFactory<?> FACTORY = new Factory<>();
    private final boolean hasFallback;

    private FurnitureItemLootEntryContainer(@Nullable Key item, List<Condition<LootContext>> conditions, List<LootFunction<T>> lootFunctions, int weight, int quality) {
        super(item, conditions, lootFunctions, weight, quality);
        this.hasFallback = item != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createItem(Consumer<Item<T>> lootConsumer, LootContext context) {
        Optional<Item<?>> optionalItem = context.getOptionalParameter(DirectContextParameters.FURNITURE_ITEM);
        if (optionalItem.isPresent()) {
            lootConsumer.accept((Item<T>) optionalItem.get());
        } else if (this.hasFallback) {
            super.createItem(lootConsumer, context);
        }
    }

    private static class Factory<A> implements LootEntryContainerFactory<A> {

        @Override
        public LootEntryContainer<A> create(Map<String, Object> arguments) {
            Key item = Optional.ofNullable(arguments.get("item")).map(String::valueOf).map(Key::of).orElse(null);
            int weight = ResourceConfigUtils.getAsInt(arguments.getOrDefault("weight", 1), "weight");
            int quality = ResourceConfigUtils.getAsInt(arguments.getOrDefault("quality", 0), "quality");
            List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            List<LootFunction<A>> functions = ResourceConfigUtils.parseConfigAsList(arguments.get("functions"), LootFunctions::fromMap);
            return new FurnitureItemLootEntryContainer<>(item, conditions, functions, weight, quality);
        }
    }
}
