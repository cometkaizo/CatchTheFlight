package com.cometkaizo.world.block;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;
import com.cometkaizo.world.entity.Entity;

public class AirBlock extends Block {

    public AirBlock(Room.Layer layer, Vector.ImmutableInt position, Args args) {
        super(layer, position, args);
    }

    @Override
    public boolean isSolid(Entity entity) {
        return false;
    }

    @Override
    public void tick() {

    }

    @Override
    protected String getTexturePath() {
        return null;
    }

}
