package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.world.Activateable;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

// button is an entity so that it can move on platforms
// todo: make button step-activated, and make luggage be able to activate it (springs back up when stepped off)
public class Button extends CollidableEntity {
    protected boolean pressed;
    protected String[] targetNames;

    public Button(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
    }

    private void onKeyPressed(KeyPressedEvent event) {
        if (event.input() == InputBindings.INTERACT.get() && isTouching(room.player)) {
            toggle();
        }
    }

    public void toggle() {
        if (!pressed) {
            pressed = true;
            activate();
        } else pressed = false;
    }

    private void activate() {
        for (String targetName : targetNames) {
            if (room.getBlockOrEntity(targetName) instanceof Activateable target) target.activate();
        }
    }

    @Override
    public void reset() {
        super.reset();
        pressed = false;
        targetNames = originalArgs.next("").split(" ");
    }

    @Override
    protected void updateBoundingBox() {
        boundingBox.position.x = position.x;
        boundingBox.position.y = position.y;
    }

    @Override
    protected double getTextureDeltaXFactor() {
        return 0;
    }
    @Override
    protected String getTexturePath() {
        return pressed ? "button_pressed" : "button";
    }
}
