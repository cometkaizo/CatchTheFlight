package com.cometkaizo.world.entity;

import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.util.MathUtils;
import com.cometkaizo.world.*;

import java.awt.*;

/**
 * one way button activated platform
 */
// TODO: make this (and other moving platforms) render below the ground base image so it looks better when moving below ground to drop off luggage
public class ButtonActivatedPlatform extends CollidableEntity implements Activateable {
    protected Direction direction;
    protected int moveAmt;
    protected double moveAmtLeft;
    protected double speed = 0.1;
    protected int fallResetDuration = 60, fallAnimStartDuration = 30, resetDuration = 20, fallSolidDuration = 30;
    protected int fallTime = -1, moveTime = -1;
    protected boolean moving;
    protected String[] attachedNames;

    public ButtonActivatedPlatform(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
    }

    @Override
    public void tick() {
        setMotion(0, 0);
        tickMotion();
        super.tick();
        tickFallTime();
    }

    private void tickMotion() {
        if (!moving || fallTime != -1) return;
        if (moveTime == 0) Assets.sound("fire").play();
        moveTime ++;
        double speed = Math.min(this.speed, moveAmtLeft);

        moveAmtLeft -= speed;
        if (moveAmtLeft < 1e-5) {
            moveAmtLeft = 0;
            startFalling();
        }

        var motion = Vector.immutable(speed * direction.x(), speed * direction.y());
        setMotion(motion);

        for (var moved : room.walls.getEntitiesWithin(boundingBox)) {
            if (moved instanceof MovableEntity movable) movable.setGroundMotion(motion);
        }
        if (room.player.boundingBox.intersects(boundingBox)) room.player.setGroundMotion(motion);
    }

    private void startFalling() {
        fallTime = 0;
        moveTime = -1;
        moving = false;
    }

    private void tickFallTime() {
        if (fallTime < -1) moveAmtLeft = moveAmt;
        if (fallTime >= fallResetDuration) {
            fallTime = -resetDuration;
            position = Vector.mutableDouble(originalPosition);
            for (String attachedName : attachedNames) {
                if (room.getBlockOrEntity(attachedName) instanceof Resettable r) r.reset();
            }
        }
        else if (fallTime != -1) {
            if (fallTime == 0) Assets.sound("crunch").play();
            if (fallTime == -resetDuration) Assets.sound("float_in").play();
            fallTime++;
        }
        if (fallTime == -2) moving = false;
    }

    @Override
    public void reset() {
        super.reset();
        direction = originalArgs.nextDirection(Direction.RIGHT);
        moveAmtLeft = moveAmt = originalArgs.nextInt(1);
        attachedNames = originalArgs.next("").split(" ");
        moving = false;
        fallTime = -1;
    }

    @Override
    public void activate() {
        moving = true;
        moveTime = 0;
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
        return "button_activated_platform";
    }

    @Override
    public boolean canCollideWhenMoving() {
        return false;
    }

    @Override
    public boolean isSolid(Entity entity) {
        return fallTime < fallSolidDuration;
    }

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();
        var oC = g.getComposite();

        // stable, wiggle, rotate + shrink
        int screenX = canvas.toScreenX(getX() + 0.5);
        int screenY = canvas.toScreenY(getY() + 0.5);

        double angle = 0;
        double translateX = 0, translateY = 0;
        double alpha = 1;

        if (fallTime >= fallAnimStartDuration) {
//            angle = Math.toRadians(Math.pow((fallTime + canvas.partialTick() - fallAnimStartDuration) * 0.2, 2.5) * 15 * deathAngleMul);
            translateY = Math.pow((fallTime + canvas.partialTick() - fallAnimStartDuration) * 0.2, 2.5) * 5;
            alpha = Math.pow(1 - (fallTime + canvas.partialTick() - fallAnimStartDuration) / (fallResetDuration - fallAnimStartDuration), 5);
        } else if (fallTime >= 10) {

        } else if (fallTime >= 2) {
            g.translate(Math.random()*6 - 3, Math.random()*6 - 3);
        } else if (fallTime <= -2) {
            int resetTime = resetDuration + fallTime + 1;
            translateY = (1 - Math.pow((resetTime + canvas.partialTick()) / resetDuration, 2.5)) * 25;
            alpha = Math.pow((resetTime + canvas.partialTick()) / resetDuration, 5);
        }

        {
            alpha = MathUtils.clamp(alpha, 0, 1);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
            g.translate(translateX, translateY);
            g.translate(screenX, screenY);
            if (angle != 0)
                g.rotate(angle);
            g.translate(-screenX, -screenY);
        }

        super.render(canvas);

        g.setTransform(oT);
        g.setComposite(oC);
    }
}
