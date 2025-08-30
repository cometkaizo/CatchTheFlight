package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.util.CollectionUtils;
import com.cometkaizo.world.Activateable;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

import java.util.Arrays;
import java.util.Collections;

public class Button extends CollidableEntity {
    protected boolean pressed;
    protected String[] targetNames;

    public Button(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
    }

    private void onKeyPressed(KeyPressedEvent event) {
        if (event.input() == InputBindings.INTERACT.get() && canBeInteracted()) {
            toggle();
            room.player.onInteract();
        }
    }

    private boolean canBeInteracted() {
        return isTouching(room.player) && room.player.canInteract();
    }

    public void toggle() {
        if (!pressed) activate();
        else deactivate();
    }

    public void activate() {
        if (pressed) return;
        pressed = true;
        for (String targetName : targetNames) {
            if (room.getBlockOrEntity(targetName) instanceof Activateable target) target.activate();
        }
        Assets.sound("click2").play();
    }

    public void deactivate() {
        if (!pressed) return;
        pressed = false;
        Assets.sound("click").play();
    }

    @Override
    public void reset() {
        super.reset();
        pressed = false;
        targetNames = originalArgs.next("").split(" ");
    }

    @Override
    protected void tickBoundingBox() {
        boundingBox.position.x = position.x;
        boundingBox.position.y = position.y;
    }

    @Override
    public boolean canBeMovedBy(Entity other) {
        return other instanceof ButtonActivatedPlatform platform && platform.isAttached(this);
    }

    @Override
    public boolean canCollideWhenMoving() {
        return false; // so that it can be in the same block as luggage on a moving platform
    }

    @Override
    protected double getTextureDeltaXFactor() {
        return 0;
    }
    @Override
    protected String getTexturePath() {
        String texture = pressed ? "button/pressed" : "button/normal";
        if (canBeInteracted()) texture += "_hovered";
        return texture;
    }
}
