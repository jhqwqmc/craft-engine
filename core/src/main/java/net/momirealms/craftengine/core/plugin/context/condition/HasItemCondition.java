package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.ItemUtils;

import java.util.Map;
import java.util.Optional;

public final class HasItemCondition<CTX extends Context> implements Condition<CTX> {

    public HasItemCondition() {
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Item<?>> item = ctx.getOptionalParameter(DirectContextParameters.ITEM_IN_HAND);
        if (item.isEmpty()) return false;
        Item<?> itemInHand = item.get();
        return !ItemUtils.isEmpty(itemInHand);
    }

    public static <CTX extends Context> ConditionFactory<CTX, HasItemCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, HasItemCondition<CTX>> {

        @Override
        public HasItemCondition<CTX> create(Map<String, Object> arguments) {
            return new HasItemCondition<>();
        }
    }
}