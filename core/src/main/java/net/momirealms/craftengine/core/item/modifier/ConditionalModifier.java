package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemDataModifierFactory;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.event.EventConditions;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ConditionalModifier<I> implements ItemDataModifier<I> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final Predicate<Context> condition;
    private final ItemDataModifier<I>[] modifiers;

    public ConditionalModifier(Predicate<Context> condition, ItemDataModifier<I>[] modifiers) {
        this.modifiers = modifiers;
        this.condition = condition;
    }

    @Override
    public Key type() {
        return ItemDataModifiers.CONDITIONAL;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (this.condition.test(context)) {
            for (ItemDataModifier<I> m : this.modifiers) {
                item = item.apply(m, context);
            }
        }
        return item;
    }

    @Override
    public Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        if (this.condition.test(context)) {
            for (ItemDataModifier<I> m : this.modifiers) {
                item = m.prepareNetworkItem(item, context, networkData);
            }
        }
        return item;
    }

    public static class Factory<I> implements ItemDataModifierFactory<I> {

        @SuppressWarnings("unchecked")
        @Override
        public ItemDataModifier<I> create(Object arg) {
            Map<String, Object> conditionalData = ResourceConfigUtils.getAsMap(arg, "conditional");
            List<Condition<Context>> conditions = ResourceConfigUtils.parseConfigAsList(conditionalData.get("conditions"), EventConditions::fromMap);
            List<ItemDataModifier<I>> modifiers = new ArrayList<>();
            ItemDataModifiers.applyDataModifiers(ResourceConfigUtils.getAsMap(conditionalData.get("data"), "conditional.data"), m -> modifiers.add((ItemDataModifier<I>) m));
            return new ConditionalModifier<>(MiscUtils.allOf(conditions), modifiers.toArray(new ItemDataModifier[0]));
        }
    }
}
