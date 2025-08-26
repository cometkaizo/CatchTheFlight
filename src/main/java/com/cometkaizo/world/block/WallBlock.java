package com.cometkaizo.world.block;

import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;
import com.cometkaizo.world.entity.Entity;

public class WallBlock extends Block {

    protected String textureVariation;

    public WallBlock(Room.Layer layer, Vector.ImmutableInt position, Args args) {
        super(layer, position, args);
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
        return textureVariation == null ? null : "wall";
    }

    @Override
    public void tick() {

    }

}
