package net.momirealms.craftengine.core.entity.furniture.element;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface ConditionalFurnitureElement extends FurnitureElement {

    @NotNull
    Predicate<PlayerContext> condition();

    @NotNull
    Furniture furniture();

    @Override
    default boolean canSee(PlayerContext context) {
        return condition().test(context);
    }

    @Override
    default void show(Player player) {
        PlayerOptionalContext context = PlayerOptionalContext.of(player, ContextHolder.builder(
                DirectContextParameters.PLAYER, player,
                DirectContextParameters.FURNITURE, furniture()
        ).build());
        if (canSee(context)) {
            showInternal(player);
        }
    }

    void showInternal(Player player);
}
