package com.cometkaizo.world.entity;

import com.cometkaizo.world.Vector;

public class BoundingBox {
    public Vector.MutableDouble position; // bottom left corner
    public Vector.ImmutableDouble size;

    public BoundingBox(Vector.MutableDouble position, Vector.ImmutableDouble size) {
        this.position = position;
        this.size = size;
    }

    public double getWidth() {
        return size.x;
    }
    public double getHeight() {
        return size.y;
    }
    public double getX() {
        return position.x;
    }
    public double getY() {
        return position.y;
    }

    public double getTop() {
        return getY() + getHeight();
    }
    public double getBottom() {
        return getY();
    }
    public double getLeft() {
        return getX();
    }
    public double getRight() {
        return getX() + getWidth();
    }

    public double getCenterX() {
        return getX() + getWidth() / 2;
    }
    public double getCenterY() {
        return getY() + getHeight() / 2;
    }

    public boolean intersects(BoundingBox other) {
        return getLeft() < other.getRight() &&
                other.getLeft() < getRight() &&
                getBottom() < other.getTop() &&
                other.getBottom() < getTop();
    }
}
