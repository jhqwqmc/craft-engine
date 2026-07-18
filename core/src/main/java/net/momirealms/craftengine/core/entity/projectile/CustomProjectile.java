package net.momirealms.craftengine.core.entity.projectile;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.item.Item;

public interface CustomProjectile {

    ProjectileMeta metadata();

    Entity projectile();

    Item item();
}
