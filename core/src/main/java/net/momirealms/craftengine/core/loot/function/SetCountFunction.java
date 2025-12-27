package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public final class SetCountFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final Key ID = Key.from("craftengine:set_count");
    public static final LootFunctionFactory<?> FACTORY = new Factory<>();
    private final NumberProvider value;
    private final boolean add;

    public SetCountFunction(List<Condition<LootContext>> conditions, NumberProvider value, boolean add) {
        super(conditions);
        this.value = value;
        this.add = add;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        int amount = this.add ? item.count() : 0;
        item.count(amount + this.value.getInt(context));
        return item;
    }

    private static class Factory<A> implements LootFunctionFactory<A> {

        @Override
        public LootFunction<A> create(Map<String, Object> arguments) {
            Object value = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("count"), "warning.config.loot_table.function.set_count.missing_count");
            boolean add = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("add", false), "add");
            List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            return new SetCountFunction<>(conditions, NumberProviders.fromObject(value), add);
        }
    }
}
