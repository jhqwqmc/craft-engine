package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.Collection;

public class AllOfFunction<CTX extends Context> implements Function<CTX> {
    private final Function<CTX>[] functions;

    public AllOfFunction(Function<CTX>[] functions) {
        this.functions = functions;
    }

    @SuppressWarnings("unchecked")
    public AllOfFunction(Collection<Function<CTX>> functions) {
        this.functions = functions.toArray(new Function[0]);
    }

    @Override
    public void run(CTX ctx) {
        for (Function<CTX> function : this.functions) {
            function.run(ctx);
        }
    }
}
