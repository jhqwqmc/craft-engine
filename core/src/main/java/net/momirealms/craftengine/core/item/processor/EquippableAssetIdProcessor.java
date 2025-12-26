package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.setting.EquipmentData;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EquippableAssetIdProcessor<I> implements SimpleNetworkItemProcessor<I> {
    private final Key assetId;

    public EquippableAssetIdProcessor(Key assetsId) {
        this.assetId = assetsId;
    }

    public Key assetId() {
        return assetId;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        Optional<EquipmentData> optionalData = item.equippable();
        optionalData.ifPresent(data -> item.equippable(new EquipmentData(
                data.slot(),
                this.assetId,
                data.dispensable(),
                data.swappable(),
                data.damageOnHurt(),
                data.equipOnInteract(),
                data.cameraOverlay()
        )));
        return item;
    }

    @Override
    public @Nullable Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.EQUIPPABLE;
    }
}
