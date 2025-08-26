package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.input.KeyBinding;
import com.cometkaizo.screen.Dialogue;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public abstract class NPCEntity extends CollidableEntity {
    public NPCEntity(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
    }

    private void onKeyPressed(KeyPressedEvent event) {
        KeyBinding input = event.input();
        if (input == InputBindings.INTERACT.get() && canBeInteracted()) {
            if (!game.hasDialogue()) game.setDialogue(newDialogue());
        }
    }

    protected boolean canBeInteracted() {
        return isTouching(room.player, interactDistance());
    }

    protected abstract Dialogue newDialogue();

    protected double interactDistance() {
        return 0;
    }
}
