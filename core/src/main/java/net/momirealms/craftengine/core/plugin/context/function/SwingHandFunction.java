package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SwingHandFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final Optional<InteractionHand> hand;

    public SwingHandFunction(List<Condition<CTX>> predicates, Optional<InteractionHand> hand) {
        super(predicates);
        this.hand = hand;
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<Player> cancellable = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        cancellable.ifPresent(value -> {
            if (this.hand.isPresent()) {
                value.swingHand(this.hand.get());
            } else {
                value.swingHand(ctx.getOptionalParameter(DirectContextParameters.HAND).orElse(InteractionHand.MAIN_HAND));
            }
        });
    }

    public static <CTX extends Context> FunctionFactory<CTX> factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            Optional<InteractionHand> optionalHand = Optional.ofNullable(arguments.get("hand")).map(it -> InteractionHand.valueOf(it.toString().toUpperCase(Locale.ENGLISH)));
            return new SwingHandFunction<>(getPredicates(arguments), optionalHand);
        }
    }
}
