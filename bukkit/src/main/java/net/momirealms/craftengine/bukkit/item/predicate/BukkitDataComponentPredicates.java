package net.momirealms.craftengine.bukkit.item.predicate;

import net.momirealms.craftengine.core.item.component.predicate.DataComponentPredicateType;
import net.momirealms.craftengine.core.item.component.predicate.DataComponentPredicates;
import net.momirealms.craftengine.core.util.Key;

public final class BukkitDataComponentPredicates extends DataComponentPredicates {
    public static final DataComponentPredicateType<ExactDataComponentPredicate> EXACT = register(Key.ce("exact"), ExactDataComponentPredicate.FACTORY);

    private BukkitDataComponentPredicates() {}

    public static void init() {
    }
}
