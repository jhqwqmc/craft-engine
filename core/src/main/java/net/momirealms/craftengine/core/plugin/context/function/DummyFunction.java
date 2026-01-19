package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Context;

public class DummyFunction<CTX extends Context> implements Function<CTX> {

    @Override
    public void run(CTX ctx) {
    }
}
