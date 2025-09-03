package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.game.event.KeyReleasedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.world.*;

import java.awt.*;

public class Button extends CollidableEntity implements AnimatedResettable {
    protected int fallResetDuration = 30, resetDuration = 20;
    protected int resetTime = -1;
    protected boolean pressed, heldDownByEntity, heldDownByPlayer;
    protected String[] targetNames;

    public Button(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
        game.getEventBus().register(KeyReleasedEvent.class, this::onKeyReleased);
    }

    private void onKeyPressed(KeyPressedEvent event) {
        if (event.input() == InputBindings.INTERACT.get() && canBeInteracted()) {
            heldDownByPlayer = true;
            room.player.onInteract();
        }
    }

    private void onKeyReleased(KeyReleasedEvent event) {
        if (event.input() == InputBindings.INTERACT.get() && isTouching(room.player)) {
            heldDownByPlayer = false;
        }
    }

    private boolean canBeInteracted() {
        return isTouching(room.player) && room.player.canInteract();
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
    public void tick() {
        super.tick();
        tickPressed();
        tickResetAnimation();
    }

    private void tickPressed() {
        if (!isTouching(room.player)) heldDownByPlayer = false;

        if (isHeldDown()) activate();
        else deactivate();

        heldDownByEntity = false;
    }

    private void tickResetAnimation() {
        if (resetTime >= fallResetDuration) {
            resetTime = -resetDuration;
            position = Vector.mutableDouble(originalPosition);
        } else if (resetTime != -1) resetTime++;
    }

    private boolean isHeldDown() {
        return heldDownByEntity || heldDownByPlayer;
    }

    @Override
    public void reset() {
        super.reset();
        pressed = false;
        heldDownByPlayer = false;
        heldDownByEntity = false;
        targetNames = originalArgs.next("").split(" ");
        resetTime = -1;
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

    public void holdDownByEntity() {
        heldDownByEntity = true;
    }

    @Override
    public void resetWithAnimation() {
        resetTime = 0;
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

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();
        var oC = g.getComposite();

        // stable, wiggle, rotate + shrink
        int screenX = canvas.toScreenX(getX() + 0.5);
        int screenY = canvas.toScreenY(getY() + 0.5);

        double translateX = 0, translateY = 0;
        double alpha = 1;

        if (resetTime >= 0) {
//            angle = Math.toRadians(Math.pow((fallTime + canvas.partialTick() - fallAnimStartDuration) * 0.2, 2.5) * 15 * deathAngleMul);
            translateY = Math.pow((resetTime + canvas.partialTick()) * 0.2, 2.5) * 5;
            alpha = Math.pow(1 - (resetTime + canvas.partialTick()) / (fallResetDuration), 5);
        } else if (resetTime >= 10) {

        } else if (resetTime >= 2) {

        } else if (resetTime <= -2) {
            int t = resetDuration + resetTime + 1;
            translateY = (1 - Math.pow((t + canvas.partialTick()) / resetDuration, 2.5)) * 25;
            alpha = Math.pow((t + canvas.partialTick()) / resetDuration, 5);
        }

        {
            alpha = Math.clamp(alpha, 0, 1);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
            g.translate(translateX, translateY);
            g.translate(screenX, screenY);
            g.translate(-screenX, -screenY);
        }

        super.render(canvas);

        g.setTransform(oT);
        g.setComposite(oC);
    }
}
