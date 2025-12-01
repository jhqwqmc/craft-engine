package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehavior;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.plugin.entityculling.CullingData;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface FurnitureConfig {

    void execute(Context context, EventTrigger trigger);

    Key id();

    FurnitureSettings settings();

    @Nullable
    LootTable<?> lootTable();

    Map<String, FurnitureVariant> variants();

    default FurnitureVariant anyVariant() {
        return variants().values().stream().findFirst().get();
    }

    default String anyVariantName() {
        return variants().keySet().stream().findFirst().get();
    }

    @Nullable
    FurnitureVariant getVariant(String variantName);

    @NotNull
    FurnitureBehavior behavior();

    CullingData cullingData();

    static Builder builder() {
        return new FurnitureConfigImpl.BuilderImpl();
    }

    interface Builder {

        Builder id(Key id);

        Builder variants(Map<String, FurnitureVariant> variants);

        Builder settings(FurnitureSettings settings);

        Builder lootTable(LootTable<?> lootTable);

        Builder events(Map<EventTrigger, List<Function<Context>>> events);

        Builder behavior(FurnitureBehavior behavior);

        Builder cullingData(CullingData cullingData);

        FurnitureConfig build();
    }
}
