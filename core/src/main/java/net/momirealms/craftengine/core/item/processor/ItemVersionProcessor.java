package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.Optional;

public final class ItemVersionProcessor implements ItemProcessor {
    public static final String VERSION_TAG = "craftengine:version";
    private final int version;

    public ItemVersionProcessor(int version) {
        this.version = version;
    }

    public int version() {
        return this.version;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (VersionHelper.isOrAbove1_20_5()) {
            CompoundTag customData = (CompoundTag) Optional.ofNullable(item.getSparrowNBTComponent(DataComponentKeys.CUSTOM_DATA)).orElseGet(CompoundTag::new);
            customData.putInt(VERSION_TAG, this.version);
            item.setNBTComponent(DataComponentKeys.CUSTOM_DATA, customData);
        } else {
            item.setTag(this.version, VERSION_TAG);
        }
        return item;
    }
}
