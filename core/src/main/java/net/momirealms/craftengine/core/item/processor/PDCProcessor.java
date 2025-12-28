package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.Map;
import java.util.Optional;

public class PDCProcessor<I> implements ItemProcessor {
    public static final String BUKKIT_PDC = "PublicBukkitValues";
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private final CompoundTag data;

    public PDCProcessor(CompoundTag data) {
        this.data = data;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (VersionHelper.isOrAbove1_20_5()) {
            CompoundTag customData = (CompoundTag) Optional.ofNullable(item.getSparrowNBTComponent(DataComponentKeys.CUSTOM_DATA)).orElseGet(CompoundTag::new);
            customData.put(BUKKIT_PDC, this.data);
            item.setNBTComponent(DataComponentKeys.CUSTOM_DATA, customData);
        } else {
            item.setTag(this.data, BUKKIT_PDC);
        }
        return item;
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "pdc");
            CompoundTag tag = (CompoundTag) CraftEngine.instance().platform().javaToSparrowNBT(data);
            return new PDCProcessor<>(tag);
        }
    }
}
