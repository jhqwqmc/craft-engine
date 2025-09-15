package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.WallTorchParticleBlockBehavior;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.SimpleContext;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.particle.ParticleConfig;

public class WallTorchParticleBlockEntity extends AbstractAnimateTickBlockEntity {
    private final WallTorchParticleBlockBehavior behavior;
    private final Context context = SimpleContext.of(ContextHolder.empty());

    public WallTorchParticleBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BukkitBlockEntityTypes.WALL_TORCH_PARTICLE, pos, blockState);
        this.behavior = blockState.behavior().getAs(WallTorchParticleBlockBehavior.class).orElseThrow();
    }

    public void animateTick(ImmutableBlockState state, World level, BlockPos pos) {
        HorizontalDirection direction = state.get(this.behavior.facingProperty);
        if (direction == null) return;
        Vec3d center = Vec3d.atCenterOf(pos);
        HorizontalDirection opposite = direction.opposite();
        for (ParticleConfig particle : this.behavior.particles) {
            Vec3d location = new Vec3d(
                    center.x() + particle.x.getDouble(context) * opposite.stepX(),
                    center.y() + particle.y.getDouble(context),
                    center.z() + particle.z.getDouble(context) * opposite.stepZ()
            );
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

    public static void tick(CEWorld ceWorld, BlockPos blockPos, ImmutableBlockState state, WallTorchParticleBlockEntity particle) {
        particle.tickCount++;
        if (particle.tickCount % particle.behavior.tickInterval != 0) return;
        particle.animateTick(state, ceWorld.world(), blockPos);
    }
}
