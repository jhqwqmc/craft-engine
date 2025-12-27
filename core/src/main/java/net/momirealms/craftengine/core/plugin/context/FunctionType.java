package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.plugin.context.function.FunctionFactory;
import net.momirealms.craftengine.core.util.Key;

public record FunctionType<CTX extends Context>(Key id, FunctionFactory<CTX> factory) {
}
