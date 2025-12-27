package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectors;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SetExpFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    private final NumberProvider count;
    private final BiConsumer<Player, Integer> operation;

    public SetExpFunction(List<Condition<CTX>> predicates, PlayerSelector<CTX> selector, NumberProvider count, BiConsumer<Player, Integer> operation) {
        super(predicates);
        this.selector = selector;
        this.count = count;
        this.operation = operation;
    }

    @Override
    protected void runInternal(CTX ctx) {
        for (Player player : this.selector.get(ctx)) {
            this.operation.accept(player, this.count.getInt(ctx));
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX> factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX> {
        private static final BiConsumer<Player, Integer> ADD_POINTS = Player::giveExperiencePoints;
        private static final BiConsumer<Player, Integer> SET_POINTS = (player, experience) -> {
            if (experience < player.getXpNeededForNextLevel()) {
                player.setExperiencePoints(experience);
            }
        };

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            PlayerSelector<CTX> selector = PlayerSelectors.fromObject(arguments.getOrDefault("target", "self"), conditionFactory());
            Object value = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("count"), "warning.config.function.set_exp.missing_count");
            boolean add = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("add", false), "add");
            return new SetExpFunction<>(getPredicates(arguments), selector, NumberProviders.fromObject(value), add ? ADD_POINTS : SET_POINTS);
        }
    }
}
