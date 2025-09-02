package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

import static com.cometkaizo.util.MathUtils.almostEquals;

public abstract class CollidableEntity extends MovableEntity {
    protected BoundingBox boundingBox;
    protected boolean collidedHorizontally;
    protected boolean collidedVertically;

    public CollidableEntity(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
    }

    @Override
    public void tick() {
        super.tick();
        tickBoundingBox();

        if (collidedVertically) motion.y = 0;
        if (collidedHorizontally) motion.x = 0;
    }

    @Override
    public void move(Vector.Double delta) {
        if (!canCollideWhenMoving()) {
            super.move(delta);
            return;
        }

        double prevX = position.x;
        double prevY = position.y;
        layer.calcAllowedMovement(position, position.addedTo(delta), this, position, canBlip());

        collidedHorizontally = !almostEquals(position.x - prevX, delta.getX());
        collidedVertically = !almostEquals(position.y - prevY, delta.getY());
    }

    protected boolean collided() {
        return collidedHorizontally || collidedVertically;
    }

    protected abstract void tickBoundingBox();

    @Override
    public void reset() {
        super.reset();
        if (boundingBox != null) tickBoundingBox();
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        if (boundingBox != null) tickBoundingBox();
    }

    public boolean isSolid(Entity entity) {
        return false;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean isTouching(CollidableEntity other) {
        return boundingBox.intersects(other.boundingBox);
    }

    public boolean isTouching(CollidableEntity other, double tolerance) {
        return boundingBox.intersects(other.boundingBox.expanded(tolerance));
    }

    public boolean isFloating() {
        return !room.ground.containsSolid(boundingBox, this);
    }
}
