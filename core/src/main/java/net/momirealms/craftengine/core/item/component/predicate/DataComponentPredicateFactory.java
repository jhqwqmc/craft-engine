package net.momirealms.craftengine.core.item.component.predicate;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface DataComponentPredicateFactory<T extends DataComponentPredicate> {

    T create(ConfigSection section);
}
