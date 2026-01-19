package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessors;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ApplyDataFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final LootFunctionFactory<?> FACTORY = new Factory<>();
    private final ItemProcessor[] modifiers;

    public ApplyDataFunction(List<Condition<LootContext>> conditions, ItemProcessor[] modifiers) {
        super(conditions);
        this.modifiers = modifiers;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        ItemBuildContext ctx = ItemBuildContext.of(context.player());
        for (ItemProcessor modifier : this.modifiers) {
            item = modifier.apply(item, ctx);
        }
        return item;
    }

    private static class Factory<A> implements LootFunctionFactory<A> {

        @Override
        public LootFunction<A> create(Map<String, Object> arguments) {
            List<ItemProcessor> modifiers = new ArrayList<>();
            Map<String, Object> data = ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("data"), "warning.config.loot_table.function.apply_data.missing_data"), "data");
            ItemProcessors.applyDataModifiers(data, modifiers::add);
            List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            return new ApplyDataFunction<>(conditions, modifiers.toArray(new ItemProcessor[0]));
        }
    }
}
