package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.util.Key;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributeManager extends Manageable {

    Optional<Attribute> getAttribute(Key id);

    Collection<Attribute> getAttributes();

    double getAttributeValue(Entity entity, Attribute attribute);

    void removeContainer(UUID uuid);

    List<AttributeOperation> sortedOperations();

    DamageFormula findFormula(DamageEvent event);

    ConfigParser[] parsers();

    void processDamageEvent(DamageEvent event);
}
