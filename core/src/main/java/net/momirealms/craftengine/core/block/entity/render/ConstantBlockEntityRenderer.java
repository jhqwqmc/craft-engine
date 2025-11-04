package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Experimental
public class ConstantBlockEntityRenderer {
    private final BlockEntityElement[] elements;

    public ConstantBlockEntityRenderer(BlockEntityElement[] elements) {
        this.elements = elements;
    }

    public void show(Player player) {
        for (BlockEntityElement element : this.elements) {
            if (element != null) {
                element.show(player);
            }
        }
    }

    public void hide(Player player) {
        for (BlockEntityElement element : this.elements) {
            if (element != null) {
                element.hide(player);
            }
        }
    }

    public void deactivate() {
        for (BlockEntityElement element : this.elements) {
            if (element != null) {
                element.deactivate();
            }
        }
    }

    public void activate() {
        for (BlockEntityElement element : this.elements) {
            if (element != null) {
                element.activate();
            }
        }
    }

    public BlockEntityElement[] elements() {
        return this.elements;
    }
}
