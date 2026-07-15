package net.momirealms.craftengine.core.item.component.predicate;

import net.momirealms.craftengine.core.item.Item;

import java.util.function.Predicate;

public interface DataComponentPredicate extends Predicate<Item> {

    void apply(Item item);
}
