package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.event.EventFunctions;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.sound.SoundSource;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RotateFurnitureFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final NumberProvider degree;
    private final List<Function<Context>> successFunctions;
    private final List<Function<Context>> failureFunctions;

    public RotateFurnitureFunction(List<Condition<CTX>> predicates, NumberProvider degree, List<Function<Context>> successFunctions, List<Function<Context>> failureFunctions) {
        super(predicates);
        this.degree = degree;
        this.successFunctions = successFunctions;
        this.failureFunctions = failureFunctions;
    }

    @Override
    public void runInternal(CTX ctx) {
        ctx.getOptionalParameter(DirectContextParameters.FURNITURE).ifPresent(furniture -> rotateFurniture(ctx, furniture));
    }

    public void rotateFurniture(CTX ctx, Furniture furniture) {
        if (!furniture.isValid()) return;
        WorldPosition position = furniture.position();
        WorldPosition newPos = new WorldPosition(position.world, position.x, position.y, position.z, position.xRot, position.yRot + this.degree.getFloat(ctx));
        furniture.moveTo(newPos).thenAccept(success -> {
            if (success) {
                for (Function<Context> successFunction : this.successFunctions) {
                    successFunction.run(ctx);
                }
            } else {
                for (Function<Context> failureFunction : this.failureFunctions) {
                    failureFunction.run(ctx);
                }
            }
        });
    }

    @Override
    public Key type() {
        return CommonFunctions.ROTATE_FURNITURE;
    }

    public NumberProvider degree() {
        return degree;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            NumberProvider degree = NumberProviders.fromObject(arguments.getOrDefault("degree", 90));
            List<Function<Context>> onSuccess = ResourceConfigUtils.parseConfigAsList(arguments.get("on-success"), EventFunctions::fromMap);
            List<Function<Context>> onFailure = ResourceConfigUtils.parseConfigAsList(arguments.get("on-failure"), EventFunctions::fromMap);
            return new RotateFurnitureFunction<>(getPredicates(arguments), degree, onSuccess, onFailure);
        }
    }
}
