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

    // it is way more important to pay attention to floating point inaccuracies than I thought
    public boolean intersects(BoundingBox other) {
        return getLeft() < other.getRight() - 1E-7 &&
                other.getLeft() < getRight() - 1E-7 &&
                getBottom() < other.getTop() - 1E-7 &&
                other.getBottom() < getTop() - 1E-7;
    }

    public boolean contains(Vector.Double position) {
        return position.getX() > getLeft() + 1E-7 && position.getX() < getRight() - 1E-7 &&
                position.getY() > getBottom() + 1E-7 && position.getY() < getTop() - 1E-7;
    }

    public BoundingBox copy() {
        return new BoundingBox(Vector.mutableDouble(position), size);
    }

    public BoundingBox expanded(double tolerance) {
        return new BoundingBox(Vector.mutable(getLeft() - tolerance, getBottom() - tolerance),
                Vector.immutable(getWidth() + tolerance * 2, getHeight() + tolerance * 2));
    }

    @Override
    public String toString() {
        return "(" + getLeft() + ", " + getBottom() + ") -> (" + getRight() + ", " + getTop() + ")";
    }
}
