package com.cometkaizo.world.block;

import java.util.Map;

public class BlockTypes {

    public static final Map<String, Block.Reader> BLOCKS = Map.of(
            ".", AirBlock::new, // explicit air block (for bottom-left anchor point)
            "", AirBlock::new,
            "g", GroundBlock::new,
            "w", WallBlock::new,
            "l", LeafBlock::new,
            "gl", GroundLeafBlock::new
    );

}
