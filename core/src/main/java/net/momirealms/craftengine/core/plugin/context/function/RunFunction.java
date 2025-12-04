package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RunFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final List<Function<CTX>> functions;
    private final NumberProvider delay;

    public RunFunction(List<Condition<CTX>> predicates, NumberProvider delay, List<Function<CTX>> functions) {
        super(predicates);
        this.functions = functions;
        this.delay = delay;
    }

    @Override
    public void runInternal(CTX ctx) {
        int delay = this.delay.getInt(ctx);
        if (delay <= 0) {
            for (Function<CTX> function : functions) {
                function.run(ctx);
            }
        } else {
            Optional<WorldPosition> position = ctx.getOptionalParameter(DirectContextParameters.POSITION);
            if (!VersionHelper.isFolia() || position.isEmpty()) {
                CraftEngine.instance().scheduler().sync().runLater(() -> {
                    for (Function<CTX> function : functions) {
                        function.run(ctx);
                    }
                }, delay);
            } else {
                WorldPosition pos = position.get();
                CraftEngine.instance().scheduler().sync().runLater(() -> {
                    for (Function<CTX> function : functions) {
                        function.run(ctx);
                    }
                }, delay, pos.world().platformWorld(), MiscUtils.floor(pos.x()) >> 4, MiscUtils.floor(pos.z()) >> 4);
            }
        }
    }

    @Override
    public Key type() {
        return CommonFunctions.RUN;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {
        private final java.util.function.Function<Map<String, Object>, Function<CTX>> functionFactory;

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Function<CTX>> functionFactory, java.util.function.Function<Map<String, Object>, Condition<CTX>> conditionFactory) {
            super(conditionFactory);
            this.functionFactory = functionFactory;
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            NumberProvider delay = NumberProviders.fromObject(arguments.getOrDefault("delay", 0));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> functions = (List<Map<String, Object>>) ResourceConfigUtils.requireNonNullOrThrow(arguments.get("functions"), "warning.config.function.run.missing_functions");
            List<Function<CTX>> fun = new ArrayList<>();
            for (Map<String, Object> function : functions) {
                fun.add(this.functionFactory.apply(function));
            }
            return new RunFunction<>(getPredicates(arguments), delay, fun);
        }
    }
}
