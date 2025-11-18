package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.ArrayUtils;
import net.momirealms.craftengine.core.util.EnumUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class HandCondition<CTX extends Context> implements Condition<CTX> {
    private final InteractionHand hand;

    public HandCondition(InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public Key type() {
        return CommonConditions.HAND;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<InteractionHand> optional = ctx.getOptionalParameter(DirectContextParameters.HAND);
        if (optional.isPresent()) {
            InteractionHand hand = optional.get();
            return hand.equals(this.hand);
        }
        return false;
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            String hand = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("hand"), "warning.config.condition.hand.missing_hand");
            try {
                return new HandCondition<>(InteractionHand.valueOf(hand.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                throw new LocalizedResourceConfigException("warning.config.condition.hand.invalid_hand", hand, EnumUtils.toString(InteractionHand.values()));
            }
        }
    }
}
