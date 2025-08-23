package com.cometkaizo.world.entity;

import com.cometkaizo.world.block.FallingPlatform;

import java.util.Map;

public class EntityTypes {

    public static final Map<String, Entity.Reader> ENTITIES = Map.of(
            "p", Player::new,
            "wolfie", InfoGuy::new,
            "lg", Luggage::new,
            "mp", MovingPlatform::new,
            "fp", FallingPlatform::new,
            "bp", ButtonActivatedPlatform::new,
            "b", Button::new
    );

}
