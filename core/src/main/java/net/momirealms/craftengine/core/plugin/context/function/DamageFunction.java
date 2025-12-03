package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public class DamageFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    private final Key damageType;
    private final NumberProvider amount;

    public DamageFunction(PlayerSelector<CTX> selector, Key damageType, NumberProvider amount, List<Condition<CTX>> predicates) {
        super(predicates);
        this.selector = selector;
        this.damageType = damageType;
        this.amount = amount;
    }

    @Override
    protected void runInternal(CTX ctx) {
        selector.get(ctx).forEach(p -> p.damage(amount.getDouble(ctx), damageType, null));
    }

    @Override
    public Key type() {
        return CommonFunctions.DAMAGE;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            PlayerSelector<CTX> selector = PlayerSelectors.fromObject(arguments.getOrDefault("target", "self"), conditionFactory());
            Key damageType = Key.of(ResourceConfigUtils.getAsStringOrNull(arguments.getOrDefault("damage-type", "generic")));
            NumberProvider amount = NumberProviders.fromObject(arguments.getOrDefault("amount", 1f));
            return new DamageFunction<>(selector, damageType, amount, getPredicates(arguments));
        }
    }
}
