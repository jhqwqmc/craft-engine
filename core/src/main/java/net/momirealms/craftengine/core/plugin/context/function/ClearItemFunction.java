package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClearItemFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final Key itemId;
    private final NumberProvider count;

    public ClearItemFunction(List<Condition<CTX>> predicates, Key itemId, NumberProvider count) {
        super(predicates);
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    protected void runInternal(CTX ctx) {
        Optional<Player> optionalPlayer = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        if (optionalPlayer.isEmpty()) {
            return;
        }
        Player player = optionalPlayer.get();
        player.clearOrCountMatchingInventoryItems(itemId, count.getInt(ctx));
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
            Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(ResourceConfigUtils.get(arguments, "id", "item"), "warning.config.function.clear_item.missing_id"));
            NumberProvider count = NumberProviders.fromObject(arguments.getOrDefault("count", 1));
            return new ClearItemFunction<>(getPredicates(arguments), itemId, count);
        }
    }
}
