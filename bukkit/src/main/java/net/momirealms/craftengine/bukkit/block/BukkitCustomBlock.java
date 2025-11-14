package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.AbstractCustomBlock;
import net.momirealms.craftengine.core.block.BlockStateVariantProvider;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.registry.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BukkitCustomBlock extends AbstractCustomBlock {

    public BukkitCustomBlock(
            @NotNull Holder.Reference<CustomBlock> holder,
            @NotNull BlockStateVariantProvider variantProvider,
            @NotNull Map<EventTrigger, List<Function<Context>>> events,
            @Nullable LootTable<?> lootTable
    ) {
        super(holder, variantProvider, events, lootTable);
    }

    @Override
    public void setPlacedBy(BlockPlaceContext context, ImmutableBlockState state) {
        try {
            this.behavior.placeMultiState(BlockStateUtils.getBlockOwner(state.customBlockState().literalObject()), new Object[]{
                    context.getLevel().serverWorld(),
                    LocationUtils.toBlockPos(context.getClickedPos()),
                    state.customBlockState().literalObject(),
                    Optional.ofNullable(context.getPlayer()).map(Player::serverPlayer).orElse(null),
                    context.getItem().getLiteralObject()
            }, () -> null);
        } catch (Throwable t) {
            CraftEngine.instance().logger().warn("Failed to run setPlacedBy ", t);
        }
    }
}
