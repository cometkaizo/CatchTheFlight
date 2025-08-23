package com.cometkaizo.world;

public enum Direction {
    UP(Axis.Y, 0, 1),
    DOWN(Axis.Y, 0, -1),
    LEFT(Axis.X, -1, 0),
    RIGHT(Axis.X, 1, 0);
    private final Axis axis;
    private final int x, y;
    Direction(Axis axis, int x, int y) {
        this.axis = axis;
        this.x = x;
        this.y = y;
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
    public static Direction ofLetter(String letter) {
        return switch (letter.toUpperCase()) {
            case "U" -> UP;
            case "D" -> DOWN;
            case "L" -> LEFT;
            case "R" -> RIGHT;
            default -> throw new IllegalArgumentException(letter);
        };
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }
}
