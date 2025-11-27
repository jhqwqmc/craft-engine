package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.collision.AABB;

public interface Cullable {

    AABB aabb();

    void show(Player player);

    void hide(Player player);
}
