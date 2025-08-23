package com.cometkaizo.world.block;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;
import com.cometkaizo.world.entity.Entity;

public class GroundBlock extends Block {

    protected String textureVariation;

    public GroundBlock(Room room, Vector.ImmutableInt position, Args args) {
        super(room, position, args);
    }

    @Override
    public boolean isSolid(Entity entity) {
        return true;
    }

    @Override
    public void reset() {
        super.reset();
        textureVariation = originalArgs.next(null);
    }

    @Override
    protected String getTexturePath() {
        return textureVariation != null ? "ground/" + textureVariation : null;
    }

    @Override
    public void tick() {

    }

}
