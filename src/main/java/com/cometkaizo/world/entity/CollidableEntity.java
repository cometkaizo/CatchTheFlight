package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class CollidableEntity extends MovableEntity {
    protected BoundingBox boundingBox;
    protected boolean isOnGround;
    protected boolean collidedHorizontally;
    protected boolean collidedVertically;

    public CollidableEntity(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
    }

    @Override
    public void tick() {
        super.tick();
        updateBoundingBox();

        isOnGround = collidedVertically && motion.y < 0;
        if (collidedVertically) motion.y = 0;
        if (collidedHorizontally) motion.x = 0;
    }

    @Override
    public void move(Vector.Double delta) {
        double prevX = position.x;
        double prevY = position.y;
        room.walls.calcAllowedMovement(position, position.addedTo(delta), boundingBox, position);

        collidedHorizontally = !epsilonEquals(position.x - prevX, delta.getX());
        collidedVertically = !epsilonEquals(position.y - prevY, delta.getY());
    }

    private static boolean epsilonEquals(double a, double b) {
        return Math.abs(b - a) < (double)1.0E-5F;
    }

    protected boolean collided() {
        return collidedHorizontally || collidedVertically;
    }

    protected abstract void updateBoundingBox();

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        updateBoundingBox();
    }
}
