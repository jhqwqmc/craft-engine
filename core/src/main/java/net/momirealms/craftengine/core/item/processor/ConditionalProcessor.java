package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ConditionalProcessor<I> implements ItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final Predicate<Context> condition;
    private final ItemProcessor<I>[] modifiers;

    public ConditionalProcessor(Predicate<Context> condition, ItemProcessor<I>[] modifiers) {
        this.modifiers = modifiers;
        this.condition = condition;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (this.condition.test(context)) {
            for (ItemProcessor<I> m : this.modifiers) {
                item = item.apply(m, context);
            }
        }
        return item;
    }

    @Override
    public Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        if (this.condition.test(context)) {
            for (ItemProcessor<I> m : this.modifiers) {
                item = m.prepareNetworkItem(item, context, networkData);
            }
        }
        return item;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @SuppressWarnings("unchecked")
        @Override
        public ItemProcessor<I> create(Object arg) {
            Map<String, Object> conditionalData = ResourceConfigUtils.getAsMap(arg, "conditional");
            List<Condition<Context>> conditions = ResourceConfigUtils.parseConfigAsList(conditionalData.get("conditions"), CommonConditions::fromMap);
            List<ItemProcessor<I>> modifiers = new ArrayList<>();
            ItemProcessors.applyDataModifiers(ResourceConfigUtils.getAsMap(conditionalData.get("data"), "conditional.data"), m -> modifiers.add((ItemProcessor<I>) m));
            return new ConditionalProcessor<>(MiscUtils.allOf(conditions), modifiers.toArray(new ItemProcessor[0]));
        }
    }
}
