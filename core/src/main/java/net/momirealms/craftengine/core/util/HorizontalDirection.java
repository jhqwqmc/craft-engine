package net.momirealms.craftengine.core.util;

import org.jetbrains.annotations.NotNull;

public enum HorizontalDirection {
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0),
    EAST(1, 0);

    private final int adjX;
    private final int adjZ;

    HorizontalDirection(int adjX, int adjZ) {
        this.adjX = adjX;
        this.adjZ = adjZ;
    }

    public int stepX() {
        return this.adjX;
    }

    public int stepZ() {
        return this.adjZ;
    }

    public Direction toDirection() {
        return switch (this) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
        };
    }

    @NotNull
    public HorizontalDirection opposite() {
        return switch (this) {
            case EAST -> WEST;
            case WEST -> EAST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
        };
    }
}
