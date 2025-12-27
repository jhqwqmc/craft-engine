package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.processor.HideTooltipProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.processor.TrimProcessor;
import net.momirealms.craftengine.core.pack.AbstractPackManager;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TrimBasedEquipment extends AbstractEquipment {
    public static final EquipmentFactory<TrimBasedEquipment> FACTORY = new Factory();
    private final Key humanoid;
    private final Key humanoidLeggings;

    public TrimBasedEquipment(Key assetId, @Nullable Key humanoid, @Nullable Key humanoidLeggings) {
        super(assetId);
        this.humanoid = humanoid;
        this.humanoidLeggings = humanoidLeggings;
    }

    @Nullable
    public Key humanoid() {
        return this.humanoid;
    }

    @Nullable
    public Key humanoidLeggings() {
        return this.humanoidLeggings;
    }

    @Override
    public <I> List<ItemProcessor<I>> modifiers() {
        return List.of(
                new TrimProcessor<>(Key.of(AbstractPackManager.NEW_TRIM_MATERIAL), this.assetId),
                new HideTooltipProcessor<>(List.of(DataComponentKeys.TRIM))
        );
    }

    private static class Factory implements EquipmentFactory<TrimBasedEquipment> {

        @Override
        public TrimBasedEquipment create(Key id, Map<String, Object> args) {
            Key humanoidId = Optional.ofNullable((String) args.get("humanoid")).map(Key::of).orElse(null);
            Key humanoidLeggingsId = Optional.ofNullable((String) args.get("humanoid-leggings")).map(Key::of).orElse(null);
            return new TrimBasedEquipment(id, humanoidId, humanoidLeggingsId);
        }
    }
}
