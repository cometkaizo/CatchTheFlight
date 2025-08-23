package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class NPCEntity extends CollidableEntity {
    public NPCEntity(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
    }
}
