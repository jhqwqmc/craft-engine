package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Immutable shape snapshots, indexed by occupied 16-block sections. Queries never access chunks. */
public final class FurnitureSpatialIndex<T> {
    private static final long MAX_SECTIONS_PER_BOX = 4096;
    private final Map<UUID, WorldIndex<T>> worlds = new ConcurrentHashMap<>();
    private final Map<T, Registration<T>> registrations = new ConcurrentHashMap<>();

    public void add(UUID worldId, T owner, List<AABB> boxes) {
        remove(owner);
        WorldIndex<T> world = this.worlds.computeIfAbsent(worldId, key -> new WorldIndex<>());
        Registration<T> registration = new Registration<>(owner, world);
        Map<Long, List<Shape<T>>> sections = new HashMap<>();
        for (int boxIndex = 0, boxCount = boxes.size(); boxIndex < boxCount; boxIndex++) {
            AABB box = boxes.get(boxIndex);
            if (!finite(box)) continue;
            Shape<T> shape = new Shape<>(registration, box);
            int minX = section(box.minX), minY = section(box.minY), minZ = section(box.minZ);
            int maxX = section(box.maxX), maxY = section(box.maxY), maxZ = section(box.maxZ);
            double count = ((double) maxX - minX + 1) * ((double) maxY - minY + 1) * ((double) maxZ - minZ + 1);
            if (count > MAX_SECTIONS_PER_BOX || minX == Integer.MIN_VALUE || maxX == Integer.MAX_VALUE
                    || minY == Integer.MIN_VALUE || maxY == Integer.MAX_VALUE
                    || minZ == Integer.MIN_VALUE || maxZ == Integer.MAX_VALUE) {
                registration.large.add(shape);
                continue;
            }
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        sections.computeIfAbsent(key(x, y, z), ignored -> new ArrayList<>()).add(shape);
                    }
                }
            }
        }
        registration.sections = sections.keySet().stream().mapToLong(Long::longValue).toArray();
        sections.forEach((key, shapes) -> world.sections.compute(key, (ignored, existing) -> {
            if (existing == null) return List.copyOf(shapes);
            List<Shape<T>> combined = new ArrayList<>(existing.size() + shapes.size());
            combined.addAll(existing);
            combined.addAll(shapes);
            return List.copyOf(combined);
        }));
        world.large.addAll(registration.large);
        registration.active = true;
        this.registrations.put(owner, registration);
    }

    public void remove(T owner) {
        Registration<T> registration = this.registrations.remove(owner);
        if (registration == null) return;
        registration.active = false;
        for (long key : registration.sections) {
            registration.world.sections.computeIfPresent(key, (ignored, shapes) -> {
                List<Shape<T>> remaining = shapes.stream().filter(shape -> shape.registration != registration).toList();
                return remaining.isEmpty() ? null : remaining;
            });
        }
        registration.large.forEach(registration.world.large::remove);
    }

    public void clear() {
        this.registrations.values().forEach(registration -> registration.active = false);
        this.registrations.clear();
        this.worlds.clear();
    }

    /** Direction must be normalized. The supplied maximum is inclusive. */
    public Hit<T> rayTrace(UUID worldId, Vec3d start, Vec3d direction, double maxDistance) {
        if (!Double.isFinite(maxDistance) || maxDistance < 0) return null;
        WorldIndex<T> world = this.worlds.get(worldId);
        if (world == null) return null;
        Search<T> search = new Search<>(start, direction, maxDistance);
        search.test(world.large);
        if (world.sections.isEmpty()) return search.result();
        int x = section(start.x), y = section(start.y), z = section(start.z);
        int stepX = sign(direction.x), stepY = sign(direction.y), stepZ = sign(direction.z);
        double nextX = boundary(start.x, direction.x, x, stepX);
        double nextY = boundary(start.y, direction.y, y, stepY);
        double nextZ = boundary(start.z, direction.z, z, stepZ);
        double deltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(16 / direction.x);
        double deltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(16 / direction.y);
        double deltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(16 / direction.z);
        while (true) {
            List<Shape<T>> shapes = world.sections.get(key(x, y, z));
            if (shapes != null) search.test(shapes);
            double next = Math.min(nextX, Math.min(nextY, nextZ));
            if (!Double.isFinite(next) || next > search.distance) break;
            if (nextX == next) { x += stepX; nextX += deltaX; }
            if (nextY == next) { y += stepY; nextY += deltaY; }
            if (nextZ == next) { z += stepZ; nextZ += deltaZ; }
        }
        return search.result();
    }

    // The slab method also handles parallel rays and starts inside a box (exit surface).
    public static double hitDistance(AABB box, Vec3d start, Vec3d direction, double maxDistance) {
        double near = Double.NEGATIVE_INFINITY, far = Double.POSITIVE_INFINITY;
        for (int axis = 0; axis < 3; axis++) {
            double origin = axis == 0 ? start.x : (axis == 1 ? start.y : start.z);
            double delta = axis == 0 ? direction.x : (axis == 1 ? direction.y : direction.z);
            double min = axis == 0 ? box.minX : axis == 1 ? box.minY : box.minZ;
            double max = axis == 0 ? box.maxX : axis == 1 ? box.maxY : box.maxZ;
            if (delta == 0) {
                if (origin < min || origin > max) return Double.POSITIVE_INFINITY;
                continue;
            }
            double a = (min - origin) / delta, b = (max - origin) / delta;
            near = Math.max(near, Math.min(a, b));
            far = Math.min(far, Math.max(a, b));
            if (near > far) return Double.POSITIVE_INFINITY;
        }
        double distance = near < 0 ? far : near;
        return distance >= 0 && distance <= maxDistance ? distance : Double.POSITIVE_INFINITY;
    }

    private static int sign(double value) {
        return value == 0 ? 0 : value > 0 ? 1 : -1;
    }

    private static double boundary(double origin, double direction, int section, int step) {
        return step == 0 ? Double.POSITIVE_INFINITY : ((section + (step > 0 ? 1d : 0d)) * 16 - origin) / direction;
    }

    private static int section(double coordinate) {
        return (int) Math.floor(coordinate / 16);
    }

    private static long key(int x, int y, int z) {
        return ((long) x & 4194303L) << 42 | (long) y & 1048575L | ((long) z & 4194303L) << 20;
    }

    private static boolean finite(AABB box) {
        return Double.isFinite(box.minX) && Double.isFinite(box.minY) && Double.isFinite(box.minZ)
                && Double.isFinite(box.maxX) && Double.isFinite(box.maxY) && Double.isFinite(box.maxZ);
    }

    public static final class Hit<T> {
        public final T owner;
        public final double distance;

        public Hit(T owner, double distance) {
            this.owner = owner;
            this.distance = distance;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof Hit<?> hit
                    && Objects.equals(this.owner, hit.owner) && Double.compare(this.distance, hit.distance) == 0;
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hashCode(this.owner) + Double.hashCode(this.distance);
        }
    }

    private static final class WorldIndex<T> {
        final Map<Long, List<Shape<T>>> sections = new ConcurrentHashMap<>();
        final Set<Shape<T>> large = ConcurrentHashMap.newKeySet();
    }

    private static final class Registration<T> {
        final T owner;
        final WorldIndex<T> world;
        final List<Shape<T>> large = new ArrayList<>();
        long[] sections;
        volatile boolean active;

        Registration(T owner, WorldIndex<T> world) {
            this.owner = owner;
            this.world = world;
        }
    }

    private record Shape<T>(Registration<T> registration, AABB box) {}

    private static final class Search<T> {
        final Set<Shape<T>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final Vec3d start;
        final Vec3d direction;
        double distance;
        Registration<T> closest;

        Search(Vec3d start, Vec3d direction, double distance) {
            this.start = start;
            this.direction = direction;
            this.distance = distance;
        }

        void test(Collection<Shape<T>> shapes) {
            for (Shape<T> shape : shapes) {
                test(shape);
            }
        }

        void test(List<Shape<T>> shapes) {
            for (int i = 0, size = shapes.size(); i < size; i++) {
                test(shapes.get(i));
            }
        }

        private void test(Shape<T> shape) {
            if (!shape.registration.active || !this.visited.add(shape)) return;
            double hit = hitDistance(shape.box, this.start, this.direction, this.distance);
            if (Double.isFinite(hit) && (this.closest == null || hit < this.distance)) {
                this.distance = hit;
                this.closest = shape.registration;
            }
        }

        Hit<T> result() {
            return this.closest == null || !this.closest.active ? null : new Hit<>(this.closest.owner, this.distance);
        }
    }
}
