package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.*;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectors;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TeleportFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    @Nullable
    private final TextProvider world;
    private final NumberProvider x;
    private final NumberProvider y;
    private final NumberProvider z;
    private final NumberProvider pitch;
    private final NumberProvider yaw;

    public TeleportFunction(List<Condition<CTX>> predicates, @Nullable PlayerSelector<CTX> selector, @Nullable TextProvider world,
                            NumberProvider x, NumberProvider y, NumberProvider z, NumberProvider pitch, NumberProvider yaw) {
        super(predicates);
        this.selector = selector;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void runInternal(CTX ctx) {
        if (this.selector == null) {
            ctx.getOptionalParameter(DirectContextParameters.PLAYER).ifPresent(it -> it.teleport(new WorldPosition(
                    Optional.ofNullable(this.world).map(w -> w.get(ctx)).map(w -> CraftEngine.instance().platform().getWorld(w)).orElse(it.world()),
                    this.x.getDouble(ctx),
                    this.y.getDouble(ctx),
                    this.z.getDouble(ctx),
                    this.pitch.getFloat(ctx),
                    this.yaw.getFloat(ctx))
            ));
        } else {
            for (Player viewer : this.selector.get(ctx)) {
                RelationalContext relationalContext = ViewerContext.of(ctx, PlayerOptionalContext.of(viewer));
                viewer.teleport(new WorldPosition(
                        Optional.ofNullable(this.world).map(w -> w.get(relationalContext)).map(w -> CraftEngine.instance().platform().getWorld(w)).orElse(viewer.world()),
                        this.x.getDouble(relationalContext),
                        this.y.getDouble(relationalContext),
                        this.z.getDouble(relationalContext),
                        this.pitch.getFloat(relationalContext),
                        this.yaw.getFloat(relationalContext))
                );
            }
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX> factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            TextProvider world = Optional.ofNullable(arguments.get("world")).map(String::valueOf).map(TextProviders::fromString).orElse(null);
            NumberProvider x = NumberProviders.fromObject(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("x"), "warning.config.function.teleport.missing_x"));
            NumberProvider y = NumberProviders.fromObject(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("y"), "warning.config.function.teleport.missing_y"));
            NumberProvider z = NumberProviders.fromObject(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("z"), "warning.config.function.teleport.missing_z"));
            NumberProvider yaw = NumberProviders.fromObject(arguments.getOrDefault("yaw", 0));
            NumberProvider pitch = NumberProviders.fromObject(arguments.getOrDefault("pitch", 0));
            return new TeleportFunction<>(
                    getPredicates(arguments),
                    PlayerSelectors.fromObject(arguments.get("target"), conditionFactory()),
                    world, x, y, z, pitch, yaw
            );
        }
    }
}
