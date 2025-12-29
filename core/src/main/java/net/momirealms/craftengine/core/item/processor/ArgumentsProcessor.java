package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.StringTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ArgumentsProcessor implements ItemProcessor {
    public static final ItemProcessorFactory<ArgumentsProcessor> FACTORY = new Factory();
    public static final String ARGUMENTS_TAG = "craftengine:arguments";
    private final Map<String, TextProvider> arguments;

    public ArgumentsProcessor(Map<String, TextProvider> arguments) {
        this.arguments = arguments;
    }

    public Map<String, TextProvider> arguments() {
        return arguments;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (VersionHelper.isOrAbove1_20_5()) {
            CompoundTag customData = (CompoundTag) Optional.ofNullable(item.getSparrowNBTComponent(DataComponentKeys.CUSTOM_DATA)).orElseGet(CompoundTag::new);
            CompoundTag argumentTag = new CompoundTag();
            for (Map.Entry<String, TextProvider> entry : this.arguments.entrySet()) {
                argumentTag.put(entry.getKey(), new StringTag(entry.getValue().get(context)));
            }
            customData.put(ARGUMENTS_TAG, argumentTag);
            item.setNBTComponent(DataComponentKeys.CUSTOM_DATA, customData);
        } else {
            Map<String, String> processed = new HashMap<>();
            for (Map.Entry<String, TextProvider> entry : this.arguments.entrySet()) {
                processed.put(entry.getKey(), entry.getValue().get(context));
            }
            item.setTag(processed, ARGUMENTS_TAG);
        }
        return item;
    }

    private static class Factory implements ItemProcessorFactory<ArgumentsProcessor> {

        @Override
        public ArgumentsProcessor create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "arguments");
            Map<String, TextProvider> arguments = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                arguments.put(entry.getKey(), TextProviders.fromString(entry.getValue().toString()));
            }
            return new ArgumentsProcessor(arguments);
        }
    }
}
