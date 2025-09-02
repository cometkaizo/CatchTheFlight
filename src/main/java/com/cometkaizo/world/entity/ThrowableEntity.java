package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class ThrowableEntity extends CollidableEntity {
    public ThrowableEntity(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
    }

    @Override
    public void tick() {
        super.tick();
        updateHeldPosition();
    }

    protected void updateHeldPosition() {
        if (isHeld()) {
            setPosition(room.player.position.x, room.player.boundingBox.getTop() + 0.1);
            if (boundingBox != null) tickBoundingBox();
        }
    }

    public boolean isHeld() {
        return room.player.isHolding();
    }

    public void launch(Vector.Double motion) {
        setMotion(motion);
    }

    @Override
    public void reset() {
        super.reset();
        if (room.player != null) {
            room.player.setHeld(this);
            updateHeldPosition();
            updateOldPosition();
        }
    }
}
