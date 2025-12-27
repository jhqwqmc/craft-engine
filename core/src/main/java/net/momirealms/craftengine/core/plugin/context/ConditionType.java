package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.util.Key;

public record ConditionType<CTX extends Context>(Key id, ConditionFactory<CTX> factory) {
}
