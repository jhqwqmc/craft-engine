package net.momirealms.craftengine.bukkit.entity.projectile;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.core.entity.projectile.AbstractCustomProjectile;
import net.momirealms.craftengine.core.entity.projectile.ProjectileMeta;
import net.momirealms.craftengine.core.item.Item;
import org.bukkit.entity.Projectile;

public final class BukkitCustomProjectile extends AbstractCustomProjectile {

    public BukkitCustomProjectile(ProjectileMeta meta, Projectile projectile, Item projectileItem) {
        super(meta, BukkitAdaptor.adapt(projectile), projectileItem);
    }
}
