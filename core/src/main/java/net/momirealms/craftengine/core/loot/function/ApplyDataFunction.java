package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifiers;
import net.momirealms.craftengine.core.loot.LootConditions;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.*;

public class ApplyDataFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final ItemDataModifier<?>[] modifiers;

    public ApplyDataFunction(List<Condition<LootContext>> conditions, ItemDataModifier<?>[] modifiers) {
        super(conditions);
        this.modifiers = modifiers;
    }

    @Override
    public Key type() {
        return LootFunctions.APPLY_DATA;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        ItemBuildContext ctx = ItemBuildContext.of(context.player());
        for (ItemDataModifier modifier : this.modifiers) {
            item = modifier.apply(item, ctx);
        }
        return item;
    }

    public static class Factory<A> implements LootFunctionFactory<A> {
        @SuppressWarnings("unchecked")
        @Override
        public LootFunction<A> create(Map<String, Object> arguments) {
            List<ItemDataModifier<?>> modifiers = new ArrayList<>();
            Map<String, Object> data = ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("data"), "warning.config.loot_table.function.apply_data.missing_data"), "data");
            ItemDataModifiers.applyDataModifiers(data, modifiers::add);
            List<Condition<LootContext>> conditions = Optional.ofNullable(arguments.get("conditions"))
                    .map(it -> LootConditions.fromMapList((List<Map<String, Object>>) it))
                    .orElse(Collections.emptyList());
            return new ApplyDataFunction<>(conditions, modifiers.toArray(new ItemDataModifier[0]));
        }
    }
}
