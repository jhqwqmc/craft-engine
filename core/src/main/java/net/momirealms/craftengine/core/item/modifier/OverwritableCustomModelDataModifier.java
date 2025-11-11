package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemDataModifierFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

public class OverwritableCustomModelDataModifier<I> implements SimpleNetworkItemDataModifier<I> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final int argument;

    public OverwritableCustomModelDataModifier(int argument) {
        this.argument = argument;
    }

    public int customModelData() {
        return this.argument;
    }

    @Override
    public Key type() {
        return ItemDataModifiers.OVERWRITABLE_CUSTOM_MODEL_DATA;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (item.customModelData().isPresent()) return item;
        item.customModelData(this.argument);
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.CUSTOM_MODEL_DATA;
    }

    @Override
    public @Nullable Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"CustomModelData"};
    }

    @Override
    public String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "CustomModelData";
    }

    public static class Factory<I> implements ItemDataModifierFactory<I> {

        @Override
        public ItemDataModifier<I> create(Object arg) {
            int customModelData = ResourceConfigUtils.getAsInt(arg, "custom-model-data");
            return new OverwritableCustomModelDataModifier<>(customModelData);
        }
    }
}
