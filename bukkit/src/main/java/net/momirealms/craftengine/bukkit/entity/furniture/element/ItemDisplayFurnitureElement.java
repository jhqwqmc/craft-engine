package net.momirealms.craftengine.bukkit.entity.furniture.element;

import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.player.Player;

public class ItemDisplayFurnitureElement implements FurnitureElement {
    private final ItemDisplayFurnitureElementConfig config;

    public ItemDisplayFurnitureElement(ItemDisplayFurnitureElementConfig config) {
        this.config = config;
    }

    @Override
    public void show(Player player) {

    }

    @Override
    public void hide(Player player) {

    }
}
