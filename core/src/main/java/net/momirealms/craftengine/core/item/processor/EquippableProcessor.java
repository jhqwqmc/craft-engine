package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.setting.EquipmentData;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EquippableProcessor<I> implements SimpleNetworkItemProcessor<I> {
    public static final Key ID = Key.of("craftengine:equippable");
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final EquipmentData data;

    public EquippableProcessor(EquipmentData data) {
        this.data = data;
    }

    public EquipmentData data() {
        return data;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        return item.equippable(this.data);
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.EQUIPPABLE;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "equippable");
            return new EquippableProcessor<>(EquipmentData.fromMap(data));
        }
    }
}
