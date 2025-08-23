package com.cometkaizo.world.entity;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public class InfoGuy extends NPCEntity {
    public InfoGuy(Room room, Vector.MutableDouble position, Args args) {
        super(room, position.add(0.5, 0D), args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(0.6D, 0.6D));
    }

    @Override
    protected void updateBoundingBox() {
        double width = boundingBox.getWidth();
        boundingBox.position.x = position.x - width / 2;
        boundingBox.position.y = position.y;
    }

    @Override
    protected String getTexturePath() {
        return "info_guy";
    }
}
