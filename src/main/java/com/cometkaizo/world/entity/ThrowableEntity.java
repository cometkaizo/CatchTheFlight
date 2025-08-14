package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class ThrowableEntity extends CollidableEntity {
    protected Entity holder;
    public ThrowableEntity(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
    }

    public boolean isHeld() {
        return holder != null;
    }

    public void launch(Vector.Double motion) {
        setMotion(motion);
        holder = null;
    }

    public void onHeldBy(Entity holder) {
        this.holder = holder;
    }
}
