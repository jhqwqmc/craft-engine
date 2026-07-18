package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeSide;
import net.momirealms.craftengine.core.attribute.DamageEvent;
import net.momirealms.craftengine.core.attribute.DamageSource;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.damage.CraftDamageSourceProxy;
import org.bukkit.event.entity.EntityDamageEvent;

public final class BukkitDamageEvent implements DamageEvent {
    private final EntityDamageEvent event;
    private final DamageSource source;
    private final BukkitAttributeManager manager;
    private Entity victim;

    public BukkitDamageEvent(BukkitAttributeManager manager, EntityDamageEvent event) {
        this.manager = manager;
        this.event = event;
        this.source = new BukkitDamageSource(CraftDamageSourceProxy.INSTANCE.getHandle(event.getDamageSource()));
    }

    @Override
    public double damage() {
        return this.event.getDamage();
    }

    @Override
    public void setDamage(double damage) {
        this.event.setDamage(damage);
    }

    @Override
    public DamageSource source() {
        return this.source;
    }

    @Override
    public Entity victim() {
        if (this.victim == null) {
            this.victim = BukkitAdaptor.adapt(this.event.getEntity());
        }
        return this.victim;
    }

    @Override
    public double getAttributeValue(AttributeSide side, Attribute attribute) {
        if (side == AttributeSide.ATTACKER) {
            Entity entity = this.source.causingEntity();
            if (entity == null) {
                return 0d;
            }
            return this.manager.getAttributeValue(entity, attribute);
        } else {
            return this.manager.getAttributeValue(victim(), attribute);
        }
    }
}
