package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.Map;

public interface FunctionFactory<CTX extends Context, T extends Function<CTX>> {

    T create(Map<String, Object> args);
}
