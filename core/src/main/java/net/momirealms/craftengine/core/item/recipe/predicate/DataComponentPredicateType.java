package net.momirealms.craftengine.core.item.recipe.predicate;

import net.momirealms.craftengine.core.util.Key;

public record DataComponentPredicateType<T extends DataComponentPredicate>(Key id, DataComponentPredicateFactory<T> factory) {
}
