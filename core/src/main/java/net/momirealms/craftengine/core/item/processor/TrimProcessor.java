package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.data.Trim;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public class TrimProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private static final Object[] NBT_PATH = new Object[] {"Trim"};
    private final Key material;
    private final Key pattern;

    public TrimProcessor(Key material, Key pattern) {
        this.material = material;
        this.pattern = pattern;
    }

    public Key material() {
        return material;
    }

    public Key pattern() {
        return pattern;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        return item.trim(new Trim(this.pattern, this.material));
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.TRIM;
    }

    @Override
    public @Nullable Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "Trim";
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "trim");
            String material = data.get("material").toString().toLowerCase(Locale.ENGLISH);
            String pattern = data.get("pattern").toString().toLowerCase(Locale.ENGLISH);
            return new TrimProcessor<>(Key.of(material), Key.of(pattern));
        }
    }
}
