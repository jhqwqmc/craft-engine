package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.Optional;

public final class PermissionCondition<CTX extends Context> implements Condition<CTX> {
    private final String permission;

    public PermissionCondition(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Player> player = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        return player.map(value -> value.hasPermission(this.permission)).orElse(false);
    }

    public static <CTX extends Context> ConditionFactory<CTX, PermissionCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, PermissionCondition<CTX>> {

        @Override
        public PermissionCondition<CTX> create(Map<String, Object> arguments) {
            String permission = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("permission"), "warning.config.condition.permission.missing_permission");
            return new PermissionCondition<>(permission);
        }
    }
}