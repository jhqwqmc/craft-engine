package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EmptyLoopEntryContainer<T> extends AbstractSingleLootEntryContainer<T> {
    public static final Key ID = Key.from("craftengine:empty");
    public static final LootEntryContainerFactory<?> FACTORY = new Factory<>();

    protected EmptyLoopEntryContainer(List<Condition<LootContext>> conditions, int weight, int quality) {
        super(conditions, null, weight, quality);
    }

    @Override
    protected void createItem(Consumer<Item<T>> lootConsumer, LootContext context) {}

    private static class Factory<A> implements LootEntryContainerFactory<A> {

        @Override
        public LootEntryContainer<A> create(Map<String, Object> arguments) {
            int weight = ResourceConfigUtils.getAsInt(arguments.getOrDefault("weight", 1), "weight");
            int quality = ResourceConfigUtils.getAsInt(arguments.getOrDefault("quality", 0), "quality");
            List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            return new EmptyLoopEntryContainer<>(conditions, weight, quality);
        }
    }
}
