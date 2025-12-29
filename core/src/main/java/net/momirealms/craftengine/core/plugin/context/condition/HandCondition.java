package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.EnumUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class HandCondition<CTX extends Context> implements Condition<CTX> {
    private final InteractionHand hand;

    public HandCondition(InteractionHand hand) {
        this.hand = hand;
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

    public static <CTX extends Context> ConditionFactory<CTX, HandCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, HandCondition<CTX>> {

        @Override
        public HandCondition<CTX> create(Map<String, Object> arguments) {
            String hand = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("hand"), "warning.config.condition.hand.missing_hand");
            try {
                return new HandCondition<>(InteractionHand.valueOf(hand.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                throw new LocalizedResourceConfigException("warning.config.condition.hand.invalid_hand", hand, EnumUtils.toString(InteractionHand.values()));
            }
        }
    }
}