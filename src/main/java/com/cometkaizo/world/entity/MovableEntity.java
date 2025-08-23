package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class MovableEntity extends Entity {
    protected Vector.MutableDouble motion = Vector.mutable(0D, 0D), groundMotion = Vector.mutable(0D, 0D);

    public MovableEntity(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
    }

    @Override
    public void tick() {
        super.tick();
        move(motion.addedTo(groundMotion));
        groundMotion.setX(0D);
        groundMotion.setY(0D);
    }

    protected void move(Vector.Double delta) {
        room.walls.calcAllowedMovement(position, position.addedTo(delta), null, position, canMoveOffLedges());
    }

    protected boolean canMoveOffLedges() {
        return true;
    }

    public Vector.Double getMotion() {
        return motion;
    }
    public void setMotion(Vector.Double motion) {
        setMotion(motion.getX(), motion.getY());
    }
    public void setMotion(double x, double y) {
        motion.setX(x);
        motion.setY(y);
    }
    public Vector.Double getGroundMotion() {
        return groundMotion;
    }
    public void setGroundMotion(Vector.Double motion) {
        setGroundMotion(motion.getX(), motion.getY());
    }
    public void setGroundMotion(double x, double y) {
        groundMotion.setX(x);
        groundMotion.setY(y);
    }
}
