package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

import java.util.List;
import java.util.Map;

public class DamageItemFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final NumberProvider amount;

    public DamageItemFunction(List<Condition<CTX>> predicates, NumberProvider amount) {
        super(predicates);
        this.amount = amount;
    }

    @Override
    protected void runInternal(CTX ctx) {
        Player player = ctx.getOptionalParameter(DirectContextParameters.PLAYER).orElse(null);
        if (player == null) return;
        Item<?> item = ctx.getOptionalParameter(DirectContextParameters.ITEM_IN_HAND).orElse(null);
        InteractionHand hand = ctx.getOptionalParameter(DirectContextParameters.HAND).orElse(null);
        if (item == null && hand != null) {
            item = player.getItemInHand(hand);
        } else if (item == null) {
            return;
        }
        EquipmentSlot slot = hand == null ? null : hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        item.hurtAndBreak(amount.getInt(ctx), player, slot);
    }

    public static class Factory<CTX extends Context> extends AbstractFactory<CTX> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            NumberProvider amount = NumberProviders.fromObject(arguments.getOrDefault("amount", 1));
            return new DamageItemFunction<>(getPredicates(arguments), amount);
        }
    }
}
