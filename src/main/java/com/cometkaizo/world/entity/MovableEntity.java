package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class MovableEntity extends Entity {
    protected Vector.MutableDouble motion = Vector.mutable(0D, 0D);

    public MovableEntity(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
    }

    @Override
    public void tick() {
        super.tick();
        move(motion);
    }

    protected void move(Vector.Double delta) {
        room.walls.calcAllowedMovement(position, position.addedTo(delta), null, position);
    }

    protected Vector.Double getMotion() {
        return motion;
    }

    protected void setMotion(Vector.Double motion) {
        setMotion(motion.getX(), motion.getY());
    }

    protected void setMotion(double x, double y) {
        motion.setX(x);
        motion.setY(y);
    }
}
