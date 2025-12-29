package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.setting.EquipmentData;
import net.momirealms.craftengine.core.util.Key;

import java.util.Optional;

public final class EquippableAssetIdProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<EquippableAssetIdProcessor> FACTORY = new Factory();
    private final Key assetId;

    public EquippableAssetIdProcessor(Key assetsId) {
        this.assetId = assetsId;
    }

    public Key assetId() {
        return this.assetId;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
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
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.EQUIPPABLE;
    }

    private static class Factory implements ItemProcessorFactory<EquippableAssetIdProcessor> {

        @Override
        public EquippableAssetIdProcessor create(Object arg) {
            String id = arg.toString();
            return new EquippableAssetIdProcessor(Key.of(id));
        }
    }
}
