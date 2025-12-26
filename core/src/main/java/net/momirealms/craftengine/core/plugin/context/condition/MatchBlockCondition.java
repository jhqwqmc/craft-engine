package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.ExistingBlock;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.*;

public final class MatchBlockCondition<CTX extends Context> implements Condition<CTX> {
    private final Set<String> ids;
    private final boolean regexMatch;
    private final NumberProvider x;
    private final NumberProvider y;
    private final NumberProvider z;

    public MatchBlockCondition(Collection<String> ids, boolean regexMatch, NumberProvider x, NumberProvider y, NumberProvider z) {
        this.ids = new HashSet<>(ids);
        this.regexMatch = regexMatch;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<WorldPosition> optionalWorldPosition = ctx.getOptionalParameter(DirectContextParameters.POSITION);
        if (optionalWorldPosition.isPresent()) {
            World world = optionalWorldPosition.get().world();
            ExistingBlock blockAt = world.getBlock(MiscUtils.floor(this.x.getDouble(ctx)), MiscUtils.floor(this.y.getDouble(ctx)), MiscUtils.floor(this.z.getDouble(ctx)));
            return MiscUtils.matchRegex(blockAt.id().asString(), this.ids, this.regexMatch);
        }
        return false;
    }

    public static <CTX extends Context> ConditionFactory<CTX> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            List<String> ids = MiscUtils.getAsStringList(arguments.get("id"));
            if (ids.isEmpty()) {
                throw new LocalizedResourceConfigException("warning.config.condition.match_block.missing_id");
            }
            boolean regex = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("regex", false), "regex");
            return new MatchBlockCondition<>(ids, regex,
                    NumberProviders.fromObject(arguments.getOrDefault("x", "<arg:position.x>")),
                    NumberProviders.fromObject(arguments.getOrDefault("y", "<arg:position.y>")),
                    NumberProviders.fromObject(arguments.getOrDefault("z", "<arg:position.z>")));
        }
    }
}
