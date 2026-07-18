package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;

public interface DamageEvent {

    double damage();

    void setDamage(double damage);

    DamageSource source();

    Entity victim();

    double getAttributeValue(AttributeSide side, Attribute attribute);
}
