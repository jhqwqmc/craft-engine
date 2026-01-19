package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.Map;

public interface ConditionFactory<CTX extends Context, T extends Condition<CTX>> {

    T create(Map<String, Object> args);
}
