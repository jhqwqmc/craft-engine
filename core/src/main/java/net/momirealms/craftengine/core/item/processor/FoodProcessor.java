package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FoodProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final Key ID = Key.of("craftengine:food");
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final int nutrition;
    private final float saturation;
    private final boolean canAlwaysEat;

    public FoodProcessor(int nutrition, float saturation, boolean canAlwaysEat) {
        this.canAlwaysEat = canAlwaysEat;
        this.nutrition = nutrition;
        this.saturation = saturation;
    }

    public boolean canAlwaysEat() {
        return canAlwaysEat;
    }

    public int nutrition() {
        return nutrition;
    }

    public float saturation() {
        return saturation;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.setJavaComponent(DataComponentKeys.FOOD, Map.of(
                "nutrition", this.nutrition,
                "saturation", this.saturation,
                "can_always_eat", this.canAlwaysEat
        ));
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.FOOD;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "food");
            int nutrition = ResourceConfigUtils.getAsInt(data.get("nutrition"), "nutrition");
            float saturation = ResourceConfigUtils.getAsFloat(data.get("saturation"), "saturation");
            return new FoodProcessor<>(nutrition, saturation, ResourceConfigUtils.getAsBoolean(data.getOrDefault("can-always-eat", false), "can-always-eat"));
        }
    }
}
