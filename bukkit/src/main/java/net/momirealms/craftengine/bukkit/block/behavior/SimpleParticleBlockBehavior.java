package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.SimpleParticleBlockEntity;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.particle.ParticleConfig;

import java.util.List;
import java.util.Map;

public class SimpleParticleBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    public final ParticleConfig[] particles;
    public final int tickInterval;

    public SimpleParticleBlockBehavior(CustomBlock customBlock, ParticleConfig[] particles, int tickInterval) {
        super(customBlock);
        this.particles = particles;
        this.tickInterval = tickInterval;
    }

    public ParticleConfig[] particles() {
        return this.particles;
    }

    public int tickInterval() {
        return tickInterval;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.SIMPLE_PARTICLE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new SimpleParticleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        if (this.particles.length == 0) return null;
        return EntityBlockBehavior.createTickerHelper(SimpleParticleBlockEntity::tick);
    }

    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            List<ParticleConfig> particles = ResourceConfigUtils.parseConfigAsList(ResourceConfigUtils.get(arguments, "particles", "particle"), ParticleConfig::fromMap$blockEntity);
            int tickInterval = ResourceConfigUtils.getAsInt(arguments.getOrDefault("tick-interval", 10), "tick-interval");
            return new SimpleParticleBlockBehavior(block, particles.toArray(new ParticleConfig[0]), tickInterval);
        }
    }
}
