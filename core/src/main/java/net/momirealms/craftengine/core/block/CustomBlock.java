package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface CustomBlock {

    Key id();

    @Nullable
    LootTable<?> lootTable();

    @NotNull
    default String translationKey() {
        Key id = id();
        return "block." + id.namespace() + "." + id.value();
    }

    void execute(Context context, EventTrigger trigger);

    @NotNull
    BlockStateVariantProvider variantProvider();

    List<ImmutableBlockState> getPossibleStates(CompoundTag nbt);

    ImmutableBlockState getBlockState(CompoundTag nbt);

    @Nullable Property<?> getProperty(String name);

    @NotNull Collection<Property<?>> properties();

    ImmutableBlockState defaultState();

    ImmutableBlockState getStateForPlacement(BlockPlaceContext context);

    void setPlacedBy(BlockPlaceContext context, ImmutableBlockState state);
}
