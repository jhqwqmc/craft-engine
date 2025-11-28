package net.momirealms.craftengine.core.plugin.entityculling;

import net.momirealms.craftengine.core.world.collision.AABB;

public final class CullingData {
    public final AABB aabb;
    public final int maxDistance;
    public final double aabbExpansion;

    public CullingData(AABB aabb, int maxDistance, double aabbExpansion) {
        this.aabb = aabb;
        this.maxDistance = maxDistance;
        this.aabbExpansion = aabbExpansion;
    }

    public AABB aabb() {
        return aabb;
    }

    public int maxDistance() {
        return maxDistance;
    }

    public double aabbExpansion() {
        return aabbExpansion;
    }
}
