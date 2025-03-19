package net.momirealms.craftengine.core.util;

public enum DoubleBlockHalf {
    UPPER(Direction.DOWN),
    LOWER(Direction.UP);

    private final Direction directionToOther;

    DoubleBlockHalf(final Direction directionToOther) {
        this.directionToOther = directionToOther;
    }

    public Direction getDirectionToOther() {
        return this.directionToOther;
    }
}
