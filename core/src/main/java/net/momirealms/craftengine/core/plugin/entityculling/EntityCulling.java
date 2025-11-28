package net.momirealms.craftengine.core.plugin.entityculling;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.MutableVec3d;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.chunk.client.ClientChunk;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.Arrays;

public final class EntityCulling {
    private final Player player;
    private final int maxDistance;
    private final double aabbExpansion;
    private final boolean[] dotSelectors = new boolean[14];
    private final MutableVec3d[] targetPoints = new MutableVec3d[14];
    
    public EntityCulling(Player player, int maxDistance, double aabbExpansion) {
        this.player = player;
        this.maxDistance = maxDistance;
        this.aabbExpansion = aabbExpansion;
        for (int i = 0; i < this.targetPoints.length; i++) {
            this.targetPoints[i] = new MutableVec3d(0,0,0);
        }
    }

    public boolean isVisible(AABB aabb, Vec3d cameraPos) {
        // 根据AABB获取能包裹此AABB的最小长方体
        int minX = MiscUtils.floor(aabb.minX - this.aabbExpansion);
        int minY = MiscUtils.floor(aabb.minY - this.aabbExpansion);
        int minZ = MiscUtils.floor(aabb.minZ - this.aabbExpansion);
        int maxX = MiscUtils.ceil(aabb.maxX + this.aabbExpansion);
        int maxY = MiscUtils.ceil(aabb.maxY + this.aabbExpansion);
        int maxZ = MiscUtils.ceil(aabb.maxZ + this.aabbExpansion);

        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        Relative relX = Relative.from(minX, maxX, cameraX);
        Relative relY = Relative.from(minY, maxY, cameraY);
        Relative relZ = Relative.from(minZ, maxZ, cameraZ);

        // 相机位于实体内部
        if (relX == Relative.INSIDE && relY == Relative.INSIDE && relZ == Relative.INSIDE) {
            return true;
        }

        // 如果设置了最大距离
        if (this.maxDistance > 0) {
            // 计算AABB到相机的最小距离
            double distanceSq = 0.0;
            // 计算XYZ轴方向的距离
            distanceSq += distanceSq(minX, maxX, cameraX, relX);
            distanceSq += distanceSq(minY, maxY, cameraY, relY);
            distanceSq += distanceSq(minZ, maxZ, cameraZ, relZ);
            // 检查距离是否超过最大值
            double maxDistanceSq = this.maxDistance * this.maxDistance;
            // 超过最大距离，剔除
            if (distanceSq > maxDistanceSq) {
                return false;
            }
        }

        // 清空之前的缓存
        Arrays.fill(this.dotSelectors, false);
        if (relX == Relative.POSITIVE) {
            this.dotSelectors[0] = this.dotSelectors[2] = this.dotSelectors[4] = this.dotSelectors[6] = this.dotSelectors[10] = true;
        } else if (relX == Relative.NEGATIVE) {
            this.dotSelectors[1] = this.dotSelectors[3] = this.dotSelectors[5] = this.dotSelectors[7] = this.dotSelectors[11] = true;
        }
        if (relY == Relative.POSITIVE) {
            this.dotSelectors[0] = this.dotSelectors[1] = this.dotSelectors[2] = this.dotSelectors[3] = this.dotSelectors[12] = true;
        } else if (relY == Relative.NEGATIVE) {
            this.dotSelectors[4] = this.dotSelectors[5] = this.dotSelectors[6] = this.dotSelectors[7] = this.dotSelectors[13] = true;
        }
        if (relZ == Relative.POSITIVE) {
            this.dotSelectors[0] = this.dotSelectors[1] = this.dotSelectors[4] = this.dotSelectors[5] = this.dotSelectors[8] = true;
        } else if (relZ == Relative.NEGATIVE) {
            this.dotSelectors[2] = this.dotSelectors[3] = this.dotSelectors[6] = this.dotSelectors[7] = this.dotSelectors[9] = true;
        }

        int size = 0;
        if (this.dotSelectors[0]) targetPoints[size++].set(minX, minY, minZ);
        if (this.dotSelectors[1]) targetPoints[size++].set(maxX, minY, minZ);
        if (this.dotSelectors[2]) targetPoints[size++].set(minX, minY, maxZ);
        if (this.dotSelectors[3]) targetPoints[size++].set(maxX, minY, maxZ);
        if (this.dotSelectors[4]) targetPoints[size++].set(minX, maxY, minZ);
        if (this.dotSelectors[5]) targetPoints[size++].set(maxX, maxY, minZ);
        if (this.dotSelectors[6]) targetPoints[size++].set(minX, maxY, maxZ);
        if (this.dotSelectors[7]) targetPoints[size++].set(maxX, maxY, maxZ);
        // 面中心点
        double averageX = (minX + maxX) / 2.0;
        double averageY = (minY + maxY) / 2.0;
        double averageZ = (minZ + maxZ) / 2.0;
        if (this.dotSelectors[8]) targetPoints[size++].set(averageX, averageY, minZ);
        if (this.dotSelectors[9]) targetPoints[size++].set(averageX, averageY, maxZ);
        if (this.dotSelectors[10]) targetPoints[size++].set(minX, averageY, averageZ);
        if (this.dotSelectors[11]) targetPoints[size++].set(maxX, averageY, averageZ);
        if (this.dotSelectors[12]) targetPoints[size++].set(averageX, minY, averageZ);
        if (this.dotSelectors[13]) targetPoints[size].set(averageX, maxY, averageZ);

        return isVisible(cameraPos, this.targetPoints, size);
    }

    /**
     * 使用3D DDA算法检测从起点到多个目标点的视线是否通畅
     * 算法基于数字微分分析，遍历射线路径上的所有方块
     */
    private boolean isVisible(Vec3d start, MutableVec3d[] targets, int targetCount) {
        // 起点所在方块的整数坐标（世界坐标转换为方块坐标）
        int startBlockX = MiscUtils.floor(start.x);
        int startBlockY = MiscUtils.floor(start.y);
        int startBlockZ = MiscUtils.floor(start.z);

        // 遍历所有目标点进行视线检测
        for (int targetIndex = 0; targetIndex < targetCount; targetIndex++) {
            MutableVec3d currentTarget = targets[targetIndex];

            // 计算起点到目标的相对向量（世界坐标差）
            double deltaX = start.x - currentTarget.x;
            double deltaY = start.y - currentTarget.y;
            double deltaZ = start.z - currentTarget.z;

            // 计算相对向量的绝对值，用于确定各方向上的距离
            double absDeltaX = Math.abs(deltaX);
            double absDeltaY = Math.abs(deltaY);
            double absDeltaZ = Math.abs(deltaZ);

            // 预计算每单位距离在各方块边界上的步进增量
            // 这些值表示射线穿过一个方块所需的时间分数
            double stepIncrementX = 1.0 / (absDeltaX + 1e-10); // 避免除0
            double stepIncrementY = 1.0 / (absDeltaY + 1e-10);
            double stepIncrementZ = 1.0 / (absDeltaZ + 1e-10);

            // 射线将穿过的总方块数量（包括起点和终点）
            int totalBlocksToCheck = 1;

            // 各方块坐标的步进方向（1: 正向, -1: 反向, 0: 静止）
            int stepDirectionX, stepDirectionY, stepDirectionZ;

            // 到下一个方块边界的时间参数（射线参数化表示）
            double nextStepTimeX, nextStepTimeY, nextStepTimeZ;

            // X方向步进参数计算
            if (absDeltaX == 0.0) {
                // X方向无变化，射线平行于YZ平面
                stepDirectionX = 0;
                nextStepTimeX = stepIncrementX;
            } else if (currentTarget.x > start.x) {
                // 目标在起点右侧，向右步进
                stepDirectionX = 1;
                totalBlocksToCheck += MiscUtils.floor(currentTarget.x) - startBlockX;
                nextStepTimeX = (startBlockX + 1 - start.x) * stepIncrementX;
            } else {
                // 目标在起点左侧，向左步进
                stepDirectionX = -1;
                totalBlocksToCheck += startBlockX - MiscUtils.floor(currentTarget.x);
                nextStepTimeX = (start.x - startBlockX) * stepIncrementX;
            }

            // Y方向步进参数计算
            if (absDeltaY == 0.0) {
                // Y方向无变化，射线平行于XZ平面
                stepDirectionY = 0;
                nextStepTimeY = stepIncrementY;
            } else if (currentTarget.y > start.y) {
                // 目标在起点上方，向上步进
                stepDirectionY = 1;
                totalBlocksToCheck += MiscUtils.floor(currentTarget.y) - startBlockY;
                nextStepTimeY = (startBlockY + 1 - start.y) * stepIncrementY;
            } else {
                // 目标在起点下方，向下步进
                stepDirectionY = -1;
                totalBlocksToCheck += startBlockY - MiscUtils.floor(currentTarget.y);
                nextStepTimeY = (start.y - startBlockY) * stepIncrementY;
            }

            // Z方向步进参数计算
            if (absDeltaZ == 0.0) {
                // Z方向无变化，射线平行于XY平面
                stepDirectionZ = 0;
                nextStepTimeZ = stepIncrementZ;
            } else if (currentTarget.z > start.z) {
                // 目标在起点前方，向前步进
                stepDirectionZ = 1;
                totalBlocksToCheck += MiscUtils.floor(currentTarget.z) - startBlockZ;
                nextStepTimeZ = (startBlockZ + 1 - start.z) * stepIncrementZ;
            } else {
                // 目标在起点后方，向后步进
                stepDirectionZ = -1;
                totalBlocksToCheck += startBlockZ - MiscUtils.floor(currentTarget.z);
                nextStepTimeZ = (start.z - startBlockZ) * stepIncrementZ;
            }

            // 执行DDA步进算法，遍历射线路径上的所有方块
            boolean isLineOfSightClear = stepRay(
                    startBlockX, startBlockY, startBlockZ,
                    stepIncrementX, stepIncrementY, stepIncrementZ, totalBlocksToCheck,
                    stepDirectionX, stepDirectionY, stepDirectionZ,
                    nextStepTimeY, nextStepTimeX, nextStepTimeZ);

            // 如果当前目标点可见立即返回
            if (isLineOfSightClear) {
                return true;
            }
        }

        return false;
    }

    private boolean stepRay(int currentBlockX, int currentBlockY, int currentBlockZ,
                            double stepSizeX, double stepSizeY, double stepSizeZ,
                            int remainingSteps, int stepDirectionX, int stepDirectionY,
                            int stepDirectionZ, double nextStepTimeY, double nextStepTimeX,
                            double nextStepTimeZ) {

        // 遍历射线路径上的所有方块（跳过最后一个目标方块）
        for (; remainingSteps > 1; remainingSteps--) {

            // 检查当前方块是否遮挡视线
            if (isOccluding(currentBlockX, currentBlockY, currentBlockZ)) {
                return false; // 视线被遮挡，立即返回
            }

            // 基于时间参数选择下一个要遍历的方块方向
            // 选择距离最近的方块边界作为下一步
            if (nextStepTimeY < nextStepTimeX && nextStepTimeY < nextStepTimeZ) {
                // Y方向边界最近，垂直移动
                currentBlockY += stepDirectionY;
                nextStepTimeY += stepSizeY;
            } else if (nextStepTimeX < nextStepTimeY && nextStepTimeX < nextStepTimeZ) {
                // X方向边界最近，水平移动
                currentBlockX += stepDirectionX;
                nextStepTimeX += stepSizeX;
            } else {
                // Z方向边界最近，深度移动
                currentBlockZ += stepDirectionZ;
                nextStepTimeZ += stepSizeZ;
            }
        }

        // 成功遍历所有中间方块，视线通畅
        return true;
    }

    private double distanceSq(int min, int max, double camera, Relative rel) {
        if (rel == Relative.NEGATIVE) {
            double dx = camera - max;
            return dx * dx;
        } else if (rel == Relative.POSITIVE) {
            double dx = min - camera;
            return dx * dx;
        }
        return 0d;
    }

    private boolean isOccluding(int x, int y, int z) {
        ClientChunk trackedChunk = this.player.getTrackedChunk(ChunkPos.asLong(x >> 4, z >> 4));
        if (trackedChunk == null) {
            return false;
        }
        return trackedChunk.isOccluding(x, y, z);
    }

    private enum Relative {
        INSIDE, POSITIVE, NEGATIVE;
        public static Relative from(int min, int max, double pos) {
            if (min > pos) return POSITIVE;
            else if (max < pos) return NEGATIVE;
            return INSIDE;
        }
    }
}
