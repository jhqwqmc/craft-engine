package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface DynamicBlockEntityRenderer {

    void show(Player player);

    void hide(Player player);

    void update(Player player);
}
