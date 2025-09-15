package net.momirealms.craftengine.core.block.entity.render.element;

import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface BlockEntityElement {

    void show(Player player);

    void hide(Player player);

    default void deactivate() {}

    default void activate() {}
}
