package net.momirealms.craftengine.bukkit.block.behavior;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldEvents;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PickaxeBlockBehavior extends FacingTriggerableBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public PickaxeBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered, Set<Key> blocks, boolean whitelistMode) {
        super(customBlock, facing, triggered, blocks, whitelistMode);
    }

    @Override
    protected Object getTickPriority() {
        return CoreReflections.instance$TickPriority$EXTREMELY_HIGH;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        tick(state, level, pos);
    }

    @Override
    public void tick(Object state, Object level, Object pos) {
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return;
        Object breakPos = FastNMS.INSTANCE.method$BlockPos$relative(pos, DirectionUtils.toNMSDirection(blockState.get(this.facingProperty)));
        Object breakState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, breakPos);
        if (blockCheckByBlockState(breakState)) {
            FastNMS.INSTANCE.method$LevelWriter$destroyBlock(level, breakPos, true, null, 512);
            tryDropItem(level, breakState, breakPos); // TODO: 这里应该在调用destroyBlock的时候被正确处理
        }
    }

    private static void tryDropItem(Object level, Object state, Object blockPos) {
        int stateId = BlockStateUtils.blockStateToId(state);
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
        if (blockState == null || blockState.isEmpty()) return;
        World world = BukkitWorldManager.instance().wrap(level);
        WorldPosition position = new WorldPosition(world, Vec3d.atCenterOf(LocationUtils.fromBlockPos(blockPos)));
        ContextHolder.Builder builder = ContextHolder.builder().withParameter(DirectContextParameters.POSITION, position);
        blockState.getDrops(builder, world, null).forEach(item -> world.dropItemNaturally(position, item));
        world.playBlockSound(position, blockState.sounds().breakSound());
        FastNMS.INSTANCE.method$Level$levelEvent(level, WorldEvents.BLOCK_BREAK_EFFECT, blockPos, stateId);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Direction> facing = (Property<Direction>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.pickaxe.missing_facing");
            Property<Boolean> triggered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("triggered"), "warning.config.block.behavior.pickaxe.missing_triggered");
            boolean whitelistMode = (boolean) arguments.getOrDefault("whitelist", false);
            Set<Key> blocks = MiscUtils.getAsStringList(arguments.get("blocks")).stream().map(Key::of).collect(Collectors.toCollection(ObjectOpenHashSet::new));
            if (blocks.isEmpty() && !whitelistMode) {
                blocks = FacingTriggerableBlockBehavior.DEFAULT_BLACKLIST_BLOCKS;
            }
            return new PickaxeBlockBehavior(block, facing, triggered, blocks, whitelistMode);
        }
    }
}
