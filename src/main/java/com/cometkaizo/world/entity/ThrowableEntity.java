package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class ThrowableEntity extends CollidableEntity {
    public ThrowableEntity(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
    }

    @Override
    public void tick() {
        super.tick();
        updateHeldPosition();
    }

    protected void updateHeldPosition() {
        if (isHeld()) {
            setPosition(room.player.position.x, room.player.boundingBox.getTop() + 0.1);
        }
    }

    public boolean isHeld() {
        return room.player.held != null;
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
