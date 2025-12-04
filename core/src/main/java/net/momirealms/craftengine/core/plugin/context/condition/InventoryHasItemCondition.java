package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.Optional;

public class InventoryHasItemCondition<CTX extends Context> implements Condition<CTX> {
    private final Key itemId;
    private final NumberProvider count;

    public InventoryHasItemCondition(Key itemId, NumberProvider count) {
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    public Key type() {
        return CommonConditions.INVENTORY_HAS_ITEM;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Player> optionalPlayer = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();
        return player.clearOrCountMatchingInventoryItems(this.itemId, 0) >= this.count.getInt(ctx);
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(ResourceConfigUtils.get(arguments, "id", "item"), "warning.config.condition.inventory_has_item.missing_id"));
            NumberProvider count = NumberProviders.fromObject(arguments.getOrDefault("count", 1));
            return new InventoryHasItemCondition<>(itemId, count);
        }
    }
}
