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

public final class ConditionalProcessor implements ItemProcessor {
    public static final ItemProcessorFactory<ConditionalProcessor> FACTORY = new Factory();
    private final Predicate<Context> condition;
    private final ItemProcessor[] modifiers;

    public ConditionalProcessor(Predicate<Context> condition, ItemProcessor[] modifiers) {
        this.modifiers = modifiers;
        this.condition = condition;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (this.condition.test(context)) {
            for (ItemProcessor m : this.modifiers) {
                item = item.apply(m, context);
            }
        }
        return item;
    }

    @Override
    public <I> Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        if (this.condition.test(context)) {
            for (ItemProcessor m : this.modifiers) {
                item = m.prepareNetworkItem(item, context, networkData);
            }
        }
        return item;
    }

    private static class Factory implements ItemProcessorFactory<ConditionalProcessor> {

        @Override
        public ConditionalProcessor create(Object arg) {
            Map<String, Object> conditionalData = ResourceConfigUtils.getAsMap(arg, "conditional");
            List<Condition<Context>> conditions = ResourceConfigUtils.parseConfigAsList(conditionalData.get("conditions"), CommonConditions::fromMap);
            List<ItemProcessor> modifiers = new ArrayList<>();
            ItemProcessors.applyDataModifiers(ResourceConfigUtils.getAsMap(conditionalData.get("data"), "conditional.data"), modifiers::add);
            return new ConditionalProcessor(MiscUtils.allOf(conditions), modifiers.toArray(new ItemProcessor[0]));
        }
    }
}
