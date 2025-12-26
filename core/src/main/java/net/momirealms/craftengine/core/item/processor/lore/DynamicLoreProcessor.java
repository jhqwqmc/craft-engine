package net.momirealms.craftengine.core.item.processor.lore;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.SimpleNetworkItemProcessor;
import net.momirealms.craftengine.core.util.Key;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class DynamicLoreProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final Key ID = Key.of("craftengine:dynamic_lore");
    public static final Factory<?> FACTORY = new Factory<>();
    public static final String CONTEXT_TAG_KEY = "craftengine:display_context";
    private final Map<String, LoreProcessor<I>> displayContexts;
    private final LoreProcessor<I> defaultModifier;

    public DynamicLoreProcessor(Map<String, LoreProcessor<I>> displayContexts) {
        this.displayContexts = displayContexts;
        this.defaultModifier = displayContexts.values().iterator().next();
    }

    public Map<String, LoreProcessor<I>> displayContexts() {
        return displayContexts;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        String displayContext = Optional.ofNullable(item.getJavaTag(CONTEXT_TAG_KEY)).orElse(this.defaultModifier).toString();
        LoreProcessor<I> lore = this.displayContexts.get(displayContext);
        if (lore == null) {
            lore = this.defaultModifier;
        }
        return lore.apply(item, context);
    }

    @Override
    public Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.LORE;
    }

    @Override
    public Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"display", "Lore"};
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "display.Lore";
    }

    public static class Factory<I> implements ItemProcessorFactory<I> {
        @Override
        public ItemProcessor<I> create(Object arg) {
            Map<String, LoreProcessor<I>> dynamicLore = new LinkedHashMap<>();
            if (arg instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    dynamicLore.put(entry.getKey().toString(), LoreProcessor.createLoreModifier(entry.getValue()));
                }
            }
            return new DynamicLoreProcessor<>(dynamicLore);
        }
    }
}
