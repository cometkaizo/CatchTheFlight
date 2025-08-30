package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.world.Activateable;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public class Crate extends CollidableEntity {

    public Crate(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
    }

    @Override
    protected void tickBoundingBox() {
        boundingBox.position.x = position.x;
        boundingBox.position.y = position.y;
    }

    @Override
    public boolean isSolid(Entity entity) {
        return true;
    }

    @Override
    protected double getTextureDeltaXFactor() {
        return 0;
    }
    @Override
    protected String getTexturePath() {
        return "crate";
    }
}
