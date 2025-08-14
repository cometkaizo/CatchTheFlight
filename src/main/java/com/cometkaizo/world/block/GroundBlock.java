package com.cometkaizo.world.block;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public class GroundBlock extends Block {

    public GroundBlock(Room room, Vector.ImmutableInt position, Args args) {
        super(room, position, args);
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    protected String getTexturePath() {
        return "stone";
    }

    @Override
    public void tick() {

    }

}
