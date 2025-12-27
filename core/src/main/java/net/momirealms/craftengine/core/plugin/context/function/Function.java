package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.List;

public interface Function<CTX extends Context> {

    void run(CTX ctx);

    static <CTX extends Context> Function<CTX> allOf(List<Function<CTX>> functions) {
        if (functions == null || functions.isEmpty()) {
            return new DummyFunction<>();
        }
        if (functions.size() == 1) {
            return functions.getFirst();
        }
        return new AllOfFunction<>(functions);
    }

    @SafeVarargs
    static <CTX extends Context> Function<CTX> allOf(Function<CTX>... functions) {
        if (functions == null || functions.length == 0) {
            return new DummyFunction<>();
        }
        if (functions.length == 1) {
            return functions[0];
        }
        return new AllOfFunction<>(functions);
    }
}
