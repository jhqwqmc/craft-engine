package net.momirealms.craftengine.core.item.recipe.predicate;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

import java.util.Collection;
import java.util.List;

public final class AllOfDataComponentPredicate implements DataComponentPredicate {
    public static final DataComponentPredicateFactory<AllOfDataComponentPredicate> FACTORY = new Factory();
    private final DataComponentPredicate[] predicates;

    public AllOfDataComponentPredicate(DataComponentPredicate[] predicates) {
        this.predicates = predicates;
    }

    public AllOfDataComponentPredicate(Collection<DataComponentPredicate> predicates) {
        this.predicates = new DataComponentPredicate[predicates.size()];
    }

    @Override
    public void apply(Item item) {
        for (DataComponentPredicate predicate : this.predicates) {
            predicate.apply(item);
        }
    }

    @Override
    public boolean test(Item item) {
        for (DataComponentPredicate predicate : this.predicates) {
            if (!predicate.test(item)) {
                return false;
            }
        }
        return true;
    }

    public static class Factory implements DataComponentPredicateFactory<AllOfDataComponentPredicate> {

        @Override
        public AllOfDataComponentPredicate create(ConfigSection section) {
            List<DataComponentPredicate> predicates = section.getSectionList("predicates", DataComponentPredicates::fromConfig);
            return new AllOfDataComponentPredicate(predicates);
        }
    }
}
