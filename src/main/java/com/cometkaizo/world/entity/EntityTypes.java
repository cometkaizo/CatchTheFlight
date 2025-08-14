package com.cometkaizo.world.entity;

import java.util.Map;

public class EntityTypes {

    public static final Map<String, Entity.Reader> ENTITIES = Map.of(
            "p", Player::new
    );

}
