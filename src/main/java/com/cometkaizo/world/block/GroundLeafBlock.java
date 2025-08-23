package com.cometkaizo.world.block;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;
import com.cometkaizo.world.entity.Entity;
import com.cometkaizo.world.entity.Player;

public class GroundLeafBlock extends Block {

    public GroundLeafBlock(Room room, Vector.ImmutableInt position, Args args) {
        super(room, position, args);
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean isSolid(Entity entity) {
        return entity instanceof Player;
    }

    @Override
    protected String getTexturePath() {
        return "ground_leaves";
    }
}
