package net.momirealms.craftengine.core.plugin.entityculling;

import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.MutableVec3d;
import net.momirealms.craftengine.core.world.Vec3d;

import java.util.Arrays;
import java.util.BitSet;

public class EntityCulling {
    
    // 面掩码常量
    private static final int ON_MIN_X = 0x01;
    private static final int ON_MAX_X = 0x02;
    private static final int ON_MIN_Y = 0x04;
    private static final int ON_MAX_Y = 0x08;
    private static final int ON_MIN_Z = 0x10;
    private static final int ON_MAX_Z = 0x20;
    
    private final int reach;
    private final double aabbExpansion;
    private final DataProvider provider;
    private final OcclusionCache cache;
    
    // 重用数据结构，减少GC压力
    private final BitSet skipList = new BitSet(); 
    private final MutableVec3d[] targetPoints = new MutableVec3d[15];
    private final MutableVec3d targetPos = new MutableVec3d(0, 0, 0);
    private final int[] cameraPos = new int[3];
    private final boolean[] dotselectors = new boolean[14];
    private final int[] lastHitBlock = new int[3];
    
    // 状态标志
    private boolean allowRayChecks = false;
    private boolean allowWallClipping = false;

    public EntityCulling(int maxDistance, DataProvider provider) {
        this(maxDistance, provider, new ArrayOcclusionCache(maxDistance), 0.5);
    }
    
    public EntityCulling(int maxDistance, DataProvider provider, OcclusionCache cache, double aabbExpansion) {
        this.reach = maxDistance;
        this.provider = provider;
        this.cache = cache;
        this.aabbExpansion = aabbExpansion;
        // 预先初始化点对象
        for(int i = 0; i < targetPoints.length; i++) {
            targetPoints[i] = new MutableVec3d(0, 0, 0);
        }
    }

    public boolean isAABBVisible(Vec3d aabbMin, MutableVec3d aabbMax, MutableVec3d viewerPosition) {
        try {
            // 计算包围盒范围
            int maxX = MiscUtils.fastFloor(aabbMax.x + aabbExpansion);
            int maxY = MiscUtils.fastFloor(aabbMax.y + aabbExpansion);
            int maxZ = MiscUtils.fastFloor(aabbMax.z + aabbExpansion);
            int minX = MiscUtils.fastFloor(aabbMin.x - aabbExpansion);
            int minY = MiscUtils.fastFloor(aabbMin.y - aabbExpansion);
            int minZ = MiscUtils.fastFloor(aabbMin.z - aabbExpansion);

            cameraPos[0] = MiscUtils.fastFloor(viewerPosition.x);
            cameraPos[1] = MiscUtils.fastFloor(viewerPosition.y);
            cameraPos[2] = MiscUtils.fastFloor(viewerPosition.z);
            
            // 判断是否在包围盒内部
            Relative relX = Relative.from(minX, maxX, cameraPos[0]);
            Relative relY = Relative.from(minY, maxY, cameraPos[1]);
            Relative relZ = Relative.from(minZ, maxZ, cameraPos[2]);
            
            if(relX == Relative.INSIDE && relY == Relative.INSIDE && relZ == Relative.INSIDE) {
                return true; 
            }
            
            skipList.clear();

            // 1. 快速检查缓存
            int id = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        int cachedValue = getCacheValue(x, y, z);
                        if (cachedValue == 1) return true; // 缓存显示可见
                        if (cachedValue != 0) skipList.set(id); // 缓存显示不可见或遮挡
                        id++;
                    }
                }
            }
            
            allowRayChecks = false;
            id = 0;
            
            // 2. 遍历体素进行光线投射检查
            for (int x = minX; x <= maxX; x++) {
                // 预计算X轴面的可见性和边缘数据
                byte visibleOnFaceX = 0;
                byte faceEdgeDataX = 0;
                if (x == minX) { faceEdgeDataX |= ON_MIN_X; if (relX == Relative.POSITIVE) visibleOnFaceX |= ON_MIN_X; }
                if (x == maxX) { faceEdgeDataX |= ON_MAX_X; if (relX == Relative.NEGATIVE) visibleOnFaceX |= ON_MAX_X; }
                
                for (int y = minY; y <= maxY; y++) {
                    byte visibleOnFaceY = visibleOnFaceX;
                    byte faceEdgeDataY = faceEdgeDataX;
                    if (y == minY) { faceEdgeDataY |= ON_MIN_Y; if (relY == Relative.POSITIVE) visibleOnFaceY |= ON_MIN_Y; }
                    if (y == maxY) { faceEdgeDataY |= ON_MAX_Y; if (relY == Relative.NEGATIVE) visibleOnFaceY |= ON_MAX_Y; }

                    for (int z = minZ; z <= maxZ; z++) {
                        // 如果缓存已标记为不可见，跳过
                        if(skipList.get(id++)) continue;

                        byte visibleOnFace = visibleOnFaceY;
                        byte faceEdgeData = faceEdgeDataY;
                        if (z == minZ) { faceEdgeData |= ON_MIN_Z; if (relZ == Relative.POSITIVE) visibleOnFace |= ON_MIN_Z; }
                        if (z == maxZ) { faceEdgeData |= ON_MAX_Z; if (relZ == Relative.NEGATIVE) visibleOnFace |= ON_MAX_Z; }
                        
                        if (visibleOnFace != 0) {
                            targetPos.set(x, y, z);
                            // 检查单个体素是否可见
                            if (isVoxelVisible(viewerPosition, targetPos, faceEdgeData, visibleOnFace)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
            return true; // 发生异常默认可见，防止渲染错误
        }
    }

    // 接口定义
    public interface DataProvider {
        boolean prepareChunk(int chunkX, int chunkZ);
        boolean isOpaqueFullCube(int x, int y, int z);
        default void cleanup() {}
        default void checkingPosition(MutableVec3d[] targetPoints, int size, MutableVec3d viewerPosition) {}
    }

    /**
     * 检查单个体素是否对观察者可见
     */
    private boolean isVoxelVisible(MutableVec3d viewerPosition, MutableVec3d position, byte faceData, byte visibleOnFace) {
        int targetSize = 0;
        Arrays.fill(dotselectors, false);
        
        // 根据相对位置选择需要检测的关键点（角点和面中心点）
        if((visibleOnFace & ON_MIN_X) != 0){
            dotselectors[0] = true;
            if((faceData & ~ON_MIN_X) != 0) { dotselectors[1] = dotselectors[4] = dotselectors[5] = true; }
            dotselectors[8] = true;
        }
        if((visibleOnFace & ON_MIN_Y) != 0){
            dotselectors[0] = true;
            if((faceData & ~ON_MIN_Y) != 0) { dotselectors[3] = dotselectors[4] = dotselectors[7] = true; }
            dotselectors[9] = true;
        }
        if((visibleOnFace & ON_MIN_Z) != 0){
            dotselectors[0] = true;
            if((faceData & ~ON_MIN_Z) != 0) { dotselectors[1] = dotselectors[4] = dotselectors[5] = true; }
            dotselectors[10] = true;
        }
        if((visibleOnFace & ON_MAX_X) != 0){
            dotselectors[4] = true;
            if((faceData & ~ON_MAX_X) != 0) { dotselectors[5] = dotselectors[6] = dotselectors[7] = true; }
            dotselectors[11] = true;
        }
        if((visibleOnFace & ON_MAX_Y) != 0){
            dotselectors[1] = true;
            if((faceData & ~ON_MAX_Y) != 0) { dotselectors[2] = dotselectors[5] = dotselectors[6] = true; }
            dotselectors[12] = true;
        }
        if((visibleOnFace & ON_MAX_Z) != 0){
            dotselectors[2] = true;
            if((faceData & ~ON_MAX_Z) != 0) { dotselectors[3] = dotselectors[6] = dotselectors[7] = true; }
            dotselectors[13] = true;
        }

        // 填充目标点，使用偏移量防止Z-Fighting或精度问题
        if (dotselectors[0]) targetPoints[targetSize++].add(position, 0.05, 0.05, 0.05);
        if (dotselectors[1]) targetPoints[targetSize++].add(position, 0.05, 0.95, 0.05);
        if (dotselectors[2]) targetPoints[targetSize++].add(position, 0.05, 0.95, 0.95);
        if (dotselectors[3]) targetPoints[targetSize++].add(position, 0.05, 0.05, 0.95);
        if (dotselectors[4]) targetPoints[targetSize++].add(position, 0.95, 0.05, 0.05);
        if (dotselectors[5]) targetPoints[targetSize++].add(position, 0.95, 0.95, 0.05);
        if (dotselectors[6]) targetPoints[targetSize++].add(position, 0.95, 0.95, 0.95);
        if (dotselectors[7]) targetPoints[targetSize++].add(position, 0.95, 0.05, 0.95);
        // 面中心点
        if (dotselectors[8]) targetPoints[targetSize++].add(position, 0.05, 0.5, 0.5);
        if (dotselectors[9]) targetPoints[targetSize++].add(position, 0.5, 0.05, 0.5);
        if (dotselectors[10]) targetPoints[targetSize++].add(position, 0.5, 0.5, 0.05);
        if (dotselectors[11]) targetPoints[targetSize++].add(position, 0.95, 0.5, 0.5);
        if (dotselectors[12]) targetPoints[targetSize++].add(position, 0.5, 0.95, 0.5);
        if (dotselectors[13]) targetPoints[targetSize++].add(position, 0.5, 0.5, 0.95);

        return isVisible(viewerPosition, targetPoints, targetSize);
    }

    // 优化：使用基本数据类型代替对象分配
    private boolean rayIntersection(int[] b, MutableVec3d rayOrigin, double dirX, double dirY, double dirZ) {
        double invX = 1.0 / dirX;
        double invY = 1.0 / dirY;
        double invZ = 1.0 / dirZ;

        double t1 = (b[0] - rayOrigin.x) * invX;
        double t2 = (b[0] + 1 - rayOrigin.x) * invX;
        double t3 = (b[1] - rayOrigin.y) * invY;
        double t4 = (b[1] + 1 - rayOrigin.y) * invY;
        double t5 = (b[2] - rayOrigin.z) * invZ;
        double t6 = (b[2] + 1 - rayOrigin.z) * invZ;

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // tmax > 0: 射线与AABB相交，但AABB在身后
        // tmin > tmax: 射线不相交
        return tmax > 0 && tmin <= tmax;
    }

    /**
     * 基于网格的光线追踪 (DDA算法)
     */
    private boolean isVisible(MutableVec3d start, MutableVec3d[] targets, int size) {
        int startX = cameraPos[0];
        int startY = cameraPos[1];
        int startZ = cameraPos[2];

        for (int v = 0; v < size; v++) {
            MutableVec3d target = targets[v];

            double relX = start.x - target.x;
            double relY = start.y - target.y;
            double relZ = start.z - target.z;

            // 优化：避免在此处创建新的Vec3d对象进行归一化
            if(allowRayChecks) {
                double len = Math.sqrt(relX * relX + relY * relY + relZ * relZ);
                // 传入归一化后的方向分量
                if (rayIntersection(lastHitBlock, start, relX / len, relY / len, relZ / len)) {
                    continue;
                }
            }
            
            double dimAbsX = Math.abs(relX);
            double dimAbsY = Math.abs(relY);
            double dimAbsZ = Math.abs(relZ);

            double dimFracX = 1f / dimAbsX;
            double dimFracY = 1f / dimAbsY;
            double dimFracZ = 1f / dimAbsZ;

            int intersectCount = 1;
            int x_inc, y_inc, z_inc;
            double t_next_y, t_next_x, t_next_z;

            // 初始化DDA步进参数
            if (dimAbsX == 0f) {
                x_inc = 0; t_next_x = dimFracX;
            } else if (target.x > start.x) {
                x_inc = 1;
                intersectCount += MiscUtils.fastFloor(target.x) - startX;
                t_next_x = (startX + 1 - start.x) * dimFracX;
            } else {
                x_inc = -1;
                intersectCount += startX - MiscUtils.fastFloor(target.x);
                t_next_x = (start.x - startX) * dimFracX;
            }

            if (dimAbsY == 0f) {
                y_inc = 0; t_next_y = dimFracY;
            } else if (target.y > start.y) {
                y_inc = 1;
                intersectCount += MiscUtils.fastFloor(target.y) - startY;
                t_next_y = (startY + 1 - start.y) * dimFracY;
            } else {
                y_inc = -1;
                intersectCount += startY - MiscUtils.fastFloor(target.y);
                t_next_y = (start.y - startY) * dimFracY;
            }

            if (dimAbsZ == 0f) {
                z_inc = 0; t_next_z = dimFracZ;
            } else if (target.z > start.z) {
                z_inc = 1;
                intersectCount += MiscUtils.fastFloor(target.z) - startZ;
                t_next_z = (startZ + 1 - start.z) * dimFracZ;
            } else {
                z_inc = -1;
                intersectCount += startZ - MiscUtils.fastFloor(target.z);
                t_next_z = (start.z - startZ) * dimFracZ;
            }

            boolean finished = stepRay(startX, startY, startZ,
                dimFracX, dimFracY, dimFracZ, intersectCount, 
                x_inc, y_inc, z_inc, 
                t_next_y, t_next_x, t_next_z);
            
            provider.cleanup();
            if (finished) {
                cacheResult(targets[0], true);
                return true;
            } else {
                allowRayChecks = true;
            }
        }
        cacheResult(targets[0], false);
        return false;
    }

    private boolean stepRay(int currentX, int currentY, int currentZ, 
                            double distInX, double distInY, double distInZ, 
                            int n, int x_inc, int y_inc, int z_inc, 
                            double t_next_y, double t_next_x, double t_next_z) {
        
        allowWallClipping = true; // 初始允许穿墙直到移出起始方块

        for (; n > 1; n--) {
            // 检查缓存状态：2=遮挡
            int cVal = getCacheValue(currentX, currentY, currentZ);
            if (cVal == 2 && !allowWallClipping) {
                lastHitBlock[0] = currentX; lastHitBlock[1] = currentY; lastHitBlock[2] = currentZ;
                return false;
            }

            if (cVal == 0) {
                // 未缓存，查询Provider
                int chunkX = currentX >> 4;
                int chunkZ = currentZ >> 4;
                if (!provider.prepareChunk(chunkX, chunkZ)) return false;

                if (provider.isOpaqueFullCube(currentX, currentY, currentZ)) {
                    if (!allowWallClipping) {
                        cache.setLastHidden();
                        lastHitBlock[0] = currentX; lastHitBlock[1] = currentY; lastHitBlock[2] = currentZ;
                        return false;
                    }
                } else {
                    allowWallClipping = false;
                    cache.setLastVisible();
                }
            } else if(cVal == 1) {
                allowWallClipping = false;
            }

            // DDA算法选择下一个体素
            if (t_next_y < t_next_x && t_next_y < t_next_z) {
                currentY += y_inc;
                t_next_y += distInY;
            } else if (t_next_x < t_next_y && t_next_x < t_next_z) {
                currentX += x_inc;
                t_next_x += distInX;
            } else {
                currentZ += z_inc;
                t_next_z += distInZ;
            }
        }
        return true;
    }

    // 缓存状态：-1=无效, 0=未检查, 1=可见, 2=遮挡
    private int getCacheValue(int x, int y, int z) {
        x -= cameraPos[0];
        y -= cameraPos[1];
        z -= cameraPos[2];
        if (Math.abs(x) > reach - 2 || Math.abs(y) > reach - 2 || Math.abs(z) > reach - 2) {
            return -1;
        }
        return cache.getState(x + reach, y + reach, z + reach);
    }

    private void cacheResult(MutableVec3d vector, boolean result) {
        int cx = MiscUtils.fastFloor(vector.x) - cameraPos[0] + reach;
        int cy = MiscUtils.fastFloor(vector.y) - cameraPos[1] + reach;
        int cz = MiscUtils.fastFloor(vector.z) - cameraPos[2] + reach;
        if (result) cache.setVisible(cx, cy, cz);
        else cache.setHidden(cx, cy, cz);
    }

    public void resetCache() {
        this.cache.resetCache();
    }

    private enum Relative {
        INSIDE, POSITIVE, NEGATIVE;
        public static Relative from(int min, int max, int pos) {
            if (max > pos && min > pos) return POSITIVE;
            else if (min < pos && max < pos) return NEGATIVE;
            return INSIDE;
        }
    }

    public interface OcclusionCache {
        void resetCache();
        void setVisible(int x, int y, int z);
        void setHidden(int x, int y, int z);
        int getState(int x, int y, int z);
        void setLastHidden();
        void setLastVisible();
    }

    // 使用位运算压缩存储状态的缓存实现
    public static class ArrayOcclusionCache implements OcclusionCache {
        private final int reachX2;
        private final byte[] cache;
        private int entry, offset;

        public ArrayOcclusionCache(int reach) {
            this.reachX2 = reach * 2;
            // 每一个位置占2位
            this.cache = new byte[(reachX2 * reachX2 * reachX2) / 4 + 1];
        }

        @Override
        public void resetCache() {
            Arrays.fill(cache, (byte) 0);
        }

        private void calcIndex(int x, int y, int z) {
            int positionKey = x + y * reachX2 + z * reachX2 * reachX2;
            entry = positionKey / 4;
            offset = (positionKey % 4) * 2;
        }

        @Override
        public void setVisible(int x, int y, int z) {
            calcIndex(x, y, z);
            cache[entry] |= 1 << offset;
        }

        @Override
        public void setHidden(int x, int y, int z) {
            calcIndex(x, y, z);
            cache[entry] |= 1 << (offset + 1);
        }

        @Override
        public int getState(int x, int y, int z) {
            calcIndex(x, y, z);
            return (cache[entry] >> offset) & 3;
        }

        @Override
        public void setLastVisible() {
            cache[entry] |= 1 << offset;
        }

        @Override
        public void setLastHidden() {
            cache[entry] |= 1 << (offset + 1);
        }
    }
}