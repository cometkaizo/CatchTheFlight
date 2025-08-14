package com.cometkaizo.world;

public enum Direction {
    UP(Axis.Y),
    DOWN(Axis.Y),
    LEFT(Axis.X),
    RIGHT(Axis.X);
    private final Axis axis;
    Direction(Axis axis) {
        this.axis = axis;
    }

    public Axis axis() {
        return axis;
    }
    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
