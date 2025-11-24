package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
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
import java.util.function.BiConsumer;

public class ExpFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    private final NumberProvider count;
    private final BiConsumer<Player, Integer> operation;

    public ExpFunction(List<Condition<CTX>> predicates, PlayerSelector<CTX> selector, NumberProvider count, BiConsumer<Player, Integer> operation) {
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

    @Override
    public Key type() {
        return CommonFunctions.EXP;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {
        private static final BiConsumer<Player, Integer> ADD_POINTS = Player::giveExperiencePoints;
        private static final BiConsumer<Player, Integer> ADD_LEVELS = Player::giveExperienceLevels;
        private static final BiConsumer<Player, Integer> SET_POINTS = (player, experience) -> {
            if (experience < player.getXpNeededForNextLevel()) {
                player.setExperiencePoints(experience);
            }
        };
        private static final BiConsumer<Player, Integer> SET_LEVELS = Player::setExperienceLevels;

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            PlayerSelector<CTX> selector = PlayerSelectors.fromObject(arguments.getOrDefault("target", "self"), conditionFactory());
            Object value = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("count"), "warning.config.function.exp.missing_count");
            boolean set = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("set", false), "set");
            boolean level = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("level", false), "level");
            BiConsumer<Player, Integer> operation = level ? (set ? SET_LEVELS : ADD_LEVELS) : (set ? SET_POINTS : ADD_POINTS);
            return new ExpFunction<>(getPredicates(arguments), selector, NumberProviders.fromObject(value), operation);
        }
    }
}
