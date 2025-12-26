package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.world.Position;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.particle.ParticleConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParticleFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final ParticleConfig config;

    public ParticleFunction(List<Condition<CTX>> predicates, ParticleConfig config) {
        super(predicates);
        this.config = config;
    }

    @Override
    public void runInternal(CTX ctx) {
        Optional<WorldPosition> optionalWorldPosition = ctx.getOptionalParameter(DirectContextParameters.POSITION);
        if (optionalWorldPosition.isPresent()) {
            World world = optionalWorldPosition.get().world();
            Position position = new Vec3d(config.x.getDouble(ctx), config.y.getDouble(ctx), config.z.getDouble(ctx));
            world.spawnParticle(position, config.particleType, config.count.getInt(ctx), config.xOffset.getDouble(ctx), config.yOffset.getDouble(ctx), config.zOffset.getDouble(ctx), config.speed.getDouble(ctx), config.particleData, ctx);
        }
    }

    public static class Factory<CTX extends Context> extends AbstractFactory<CTX> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            return new ParticleFunction<>(getPredicates(arguments), ParticleConfig.fromMap$function(arguments));
        }
    }
}
