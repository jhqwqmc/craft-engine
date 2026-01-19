package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.joml.Vector3f;

public final class DyedColorProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<DyedColorProcessor> FACTORY = new Factory();
    private static final Object[] NBT_PATH = new Object[]{"display", "color"};
    private final Color color;

    public DyedColorProcessor(Color color) {
        this.color = color;
    }

    public Color dyedColor() {
        return color;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        return item.dyedColor(this.color);
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.DYED_COLOR;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "display.color";
    }

    private static class Factory implements ItemProcessorFactory<DyedColorProcessor> {

        @Override
        public DyedColorProcessor create(Object arg) {
            if (arg instanceof Integer integer) {
                return new DyedColorProcessor(Color.fromDecimal(integer));
            } else {
                Vector3f vector3f = ResourceConfigUtils.getAsVector3f(arg, "dyed-color");
                return new DyedColorProcessor(Color.fromVector3f(vector3f));
            }
        }
    }
}
