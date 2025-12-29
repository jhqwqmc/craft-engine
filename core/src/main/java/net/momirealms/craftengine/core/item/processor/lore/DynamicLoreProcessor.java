package net.momirealms.craftengine.core.item.processor.lore;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.processor.SimpleNetworkItemProcessor;
import net.momirealms.craftengine.core.util.Key;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class DynamicLoreProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<DynamicLoreProcessor> FACTORY = new Factory();
    public static final String CONTEXT_TAG_KEY = "craftengine:display_context";
    private final Map<String, LoreProcessor> displayContexts;
    private final LoreProcessor defaultModifier;

    public DynamicLoreProcessor(Map<String, LoreProcessor> displayContexts) {
        this.displayContexts = displayContexts;
        this.defaultModifier = displayContexts.values().iterator().next();
    }

    public Map<String, LoreProcessor> displayContexts() {
        return displayContexts;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        String displayContext = Optional.ofNullable(item.getJavaTag(CONTEXT_TAG_KEY)).orElse(this.defaultModifier).toString();
        LoreProcessor lore = this.displayContexts.get(displayContext);
        if (lore == null) {
            lore = this.defaultModifier;
        }
        return lore.apply(item, context);
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.LORE;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"display", "Lore"};
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "display.Lore";
    }

    private static class Factory implements ItemProcessorFactory<DynamicLoreProcessor> {
        @Override
        public DynamicLoreProcessor create(Object arg) {
            Map<String, LoreProcessor> dynamicLore = new LinkedHashMap<>();
            if (arg instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    dynamicLore.put(entry.getKey().toString(), LoreProcessor.createLoreModifier(entry.getValue()));
                }
            }
            return new DynamicLoreProcessor(dynamicLore);
        }
    }
}
