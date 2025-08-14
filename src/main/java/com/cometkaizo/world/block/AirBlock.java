package com.cometkaizo.world.block;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public class AirBlock extends Block {

    public AirBlock(Room room, Vector.ImmutableInt position, Args args) {
        super(room, position, args);
    }

    @Override
    public boolean isSolid() {
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
