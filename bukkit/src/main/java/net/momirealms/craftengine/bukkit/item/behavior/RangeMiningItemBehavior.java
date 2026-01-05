package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3i;
import net.momirealms.craftengine.core.world.World;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RangeMiningItemBehavior extends ItemBehavior {
    public static final ItemBehaviorFactory<RangeMiningItemBehavior> FACTORY = new Factory();
    private final List<Vec3i> miningRange;

    private RangeMiningItemBehavior(List<Vec3i> miningRange) {
        this.miningRange = miningRange;
    }

    @Override
    public void breakBlock(World world, Player player, BlockPos pos) {
        BukkitServerPlayer serverPlayer = (BukkitServerPlayer) player;
        if (serverPlayer.isRangeMining()) return;
        BlockStateWrapper blockState = world.getBlockState(pos);
        float destroyProgress = player.getDestroyProgress(blockState.literalObject(), pos);
        Direction facing = player.getDirection();
        if (player.xRot() > 45) {
            facing = Direction.UP;
        } else if (player.xRot() < -45) {
            facing = Direction.DOWN;
        }
        serverPlayer.setRangeMining(true);
        try {
            for (Vec3i offset : this.miningRange) {
                // 根据玩家朝向旋转偏移量
                Vec3i rotatedOffset = rotateOffsetByFacing(offset, facing);
                // 计算目标位置
                int targetX = pos.x() + rotatedOffset.x();
                int targetY = pos.y() + rotatedOffset.y();
                int targetZ = pos.z() + rotatedOffset.z();
                // 获取目标位置的方块状态
                BlockPos targetPos = new BlockPos(targetX, targetY, targetZ);
                BlockStateWrapper targetBlockState = world.getBlockState(targetPos);
                if (targetBlockState != null && !targetBlockState.isAir()) {
                    // 获取目标方块的硬度
                    float targetHardness = player.getDestroyProgress(targetBlockState.literalObject(), targetPos);
                    // 如果方块的硬度小于或等于破坏进度，则执行破坏
                    if (targetHardness <= destroyProgress) {
                        player.breakBlock(targetX, targetY, targetZ);
                    }
                }
            }
        } finally {
            serverPlayer.setRangeMining(false);
        }
    }

    /**
     * 根据玩家朝向旋转偏移量
     *
     * @param offset 原始偏移量
     * @param facing 玩家朝向
     * @return 旋转后的偏移量
     */
    private Vec3i rotateOffsetByFacing(Vec3i offset, Direction facing) {
        int x = offset.x();
        int y = offset.y();
        int z = offset.z();
        return switch (facing) {
            case NORTH ->
                // 北方向，默认朝向，不需要旋转
                    new Vec3i(x, y, z);
            case SOUTH ->
                // 南方向，180度旋转
                    new Vec3i(-x, y, -z);
            case EAST ->
                // 东方向，顺时针90度旋转
                    new Vec3i(-z, y, x);
            case WEST ->
                // 西方向，逆时针90度旋转
                    new Vec3i(z, y, -x);
            case UP ->
                // 上方向（特殊情况，可能处理挖掘上方）
                    new Vec3i(x, z, y);
            case DOWN ->
                // 下方向（特殊情况，可能处理挖掘下方）
                    new Vec3i(x, -z, -y);
            default -> offset;
        };
    }

    /**
     * 获取挖掘范围（用于调试或显示）
     *
     * @return 挖掘范围列表
     */
    public List<Vec3i> getMiningRange() {
        return this.miningRange;
    }

    private static class Factory implements ItemBehaviorFactory<RangeMiningItemBehavior> {
        @Override
        public RangeMiningItemBehavior create(Pack pack, Path path, String node, Key key, Map<String, Object> arguments) {
            List<String> poses = MiscUtils.getAsStringList(arguments.get("range"));
            List<Vec3i> range = poses.stream().map(it -> {
                String[] split = it.split(",", 3);
                if (split.length != 3) {
                    return null;
                }
                return new Vec3i(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            }).filter(Objects::nonNull).toList();
            return new RangeMiningItemBehavior(range);
        }
    }
}