package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.Cullable;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class ConstantBlockEntityRenderer implements Cullable {
    private final BlockEntityElement[] elements;
    public final AABB aabb;

    public ConstantBlockEntityRenderer(BlockEntityElement[] elements, AABB aabb) {
        this.elements = elements;
        this.aabb = aabb;
    }

    @Override
    public void show(Player player) {
        for (BlockEntityElement element : this.elements) {
            if (element != null) {
                element.show(player);
            }
        }
    }

    @Override
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

    @Override
    public AABB aabb() {
        return this.aabb;
    }
}
