package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.*;

public final class MatchEntityCondition<CTX extends Context> implements Condition<CTX> {
    private final Set<String> ids;
    private final boolean regexMatch;

    public MatchEntityCondition(Collection<String> ids, boolean regexMatch) {
        this.ids = new HashSet<>(ids);
        this.regexMatch = regexMatch;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Entity> entity = ctx.getOptionalParameter(DirectContextParameters.ENTITY);
        return entity.filter(value -> MiscUtils.matchRegex(value.type().asString(), this.ids, this.regexMatch)).isPresent();
    }

    public static <CTX extends Context> ConditionFactory<CTX, MatchEntityCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, MatchEntityCondition<CTX>> {

        @Override
        public MatchEntityCondition<CTX> create(Map<String, Object> arguments) {
            List<String> ids = MiscUtils.getAsStringList(arguments.get("id"));
            if (ids.isEmpty()) {
                throw new LocalizedResourceConfigException("warning.config.condition.match_entity.missing_id");
            }
            boolean regex = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("regex", false), "regex");
            return new MatchEntityCondition<>(ids, regex);
        }
    }
}