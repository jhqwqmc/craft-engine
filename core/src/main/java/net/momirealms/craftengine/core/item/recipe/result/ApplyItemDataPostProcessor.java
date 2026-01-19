package net.momirealms.craftengine.core.item.recipe.result;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessors;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplyItemDataPostProcessor implements PostProcessor {
    public static final PostProcessorFactory<ApplyItemDataPostProcessor> FACTORY = new Factory();
    private final ItemProcessor[] modifiers;

    public ApplyItemDataPostProcessor(ItemProcessor[] modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public <I> Item<I> process(Item<I> item, ItemBuildContext context) {
        for (ItemProcessor modifier : this.modifiers) {
            item.apply(modifier, context);
        }
        return item;
    }

    private static class Factory implements PostProcessorFactory<ApplyItemDataPostProcessor> {

        @Override
        public ApplyItemDataPostProcessor create(Map<String, Object> args) {
            List<ItemProcessor> modifiers = new ArrayList<>();
            Map<String, Object> data = ResourceConfigUtils.getAsMap(args.get("data"), "data");
            ItemProcessors.applyDataModifiers(data, modifiers::add);
            return new ApplyItemDataPostProcessor(modifiers.toArray(new ItemProcessor[0]));
        }
    }
}