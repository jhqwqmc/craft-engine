package net.momirealms.craftengine.bukkit.item.recipe.predicate;

import net.momirealms.craftengine.core.item.recipe.predicate.DataComponentPredicateType;
import net.momirealms.craftengine.core.item.recipe.predicate.DataComponentPredicates;
import net.momirealms.craftengine.core.util.Key;

public final class BukkitDataComponentPredicates extends DataComponentPredicates {
    public static final DataComponentPredicateType<ExactDataComponentPredicate> EXACT = register(Key.ce("exact"), ExactDataComponentPredicate.FACTORY);

    private BukkitDataComponentPredicates() {}

    public static void init() {
    }
}
