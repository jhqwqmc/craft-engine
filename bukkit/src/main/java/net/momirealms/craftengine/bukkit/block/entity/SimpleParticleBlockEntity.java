package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.SimpleParticleBlockBehavior;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.SimpleContext;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.particle.ParticleConfig;

public class SimpleParticleBlockEntity extends AbstractAnimateTickBlockEntity {
    private final SimpleParticleBlockBehavior behavior;
    private final Context context = SimpleContext.of(ContextHolder.empty());

    public SimpleParticleBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BukkitBlockEntityTypes.SIMPLE_PARTICLE, pos, blockState);
        this.behavior = blockState.behavior().getAs(SimpleParticleBlockBehavior.class).orElseThrow();
    }

    public void animateTick(ImmutableBlockState state, World level, BlockPos pos) {
        for (ParticleConfig particle : this.behavior.particles) {
            Vec3d location = new Vec3d(super.pos.x() + particle.x.getDouble(context), super.pos.y() + particle.y.getDouble(context), super.pos.z() + particle.z.getDouble(context));
            level.spawnParticle(
                    location,
                    particle.particleType,
                    particle.count.getInt(context),
                    particle.xOffset.getDouble(context),
                    particle.yOffset.getDouble(context),
                    particle.zOffset.getDouble(context),
                    particle.speed.getDouble(context),
                    particle.particleData,
                    context
            );
        }
    }

    public static void tick(CEWorld ceWorld, BlockPos blockPos, ImmutableBlockState state, SimpleParticleBlockEntity particle) {
        particle.tickCount++;
        if (particle.tickCount % particle.behavior.tickInterval != 0) return;
        particle.animateTick(state, ceWorld.world(), blockPos);
    }
}
