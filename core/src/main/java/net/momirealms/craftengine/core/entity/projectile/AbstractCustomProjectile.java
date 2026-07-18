package net.momirealms.craftengine.core.entity.projectile;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.item.Item;

public abstract class AbstractCustomProjectile implements CustomProjectile {
    protected final ProjectileMeta meta;
    protected final Entity projectile;
    protected final Item item;

    protected AbstractCustomProjectile(ProjectileMeta meta, Entity projectile, Item item) {
        this.meta = meta;
        this.projectile = projectile;
        this.item = item;
    }

    @Override
    public ProjectileMeta metadata() {
        return this.meta;
    }

    @Override
    public Entity projectile() {
        return this.projectile;
    }

    @Override
    public Item item() {
        return this.item;
    }
}
