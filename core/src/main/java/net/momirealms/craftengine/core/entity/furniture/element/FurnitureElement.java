package net.momirealms.craftengine.core.entity.furniture.element;

import net.momirealms.craftengine.core.entity.player.Player;

public interface FurnitureElement {

    void show(Player player);

    void hide(Player player);

    default void deactivate() {}

    default void activate() {}
}
