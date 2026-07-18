package net.momirealms.craftengine.core.attribute;

public interface DamageEvent {

    double damage();

    DamageSource source();

    void setDamage(double damage);

    double getAttributeValue(AttributeSide side, Attribute attribute);
}
