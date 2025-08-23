package com.cometkaizo.world.block;

import com.cometkaizo.screen.Canvas;
import com.cometkaizo.util.MathUtils;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;
import com.cometkaizo.world.entity.BoundingBox;
import com.cometkaizo.world.entity.CollidableEntity;
import com.cometkaizo.world.entity.Entity;

import java.awt.*;

public class FallingPlatform extends CollidableEntity {

    protected int fallResetDuration = 60, fallAnimStartDuration = 30, resetDuration = 20, fallSolidDuration = 30;
    protected int fallTime = -1;

    public FallingPlatform(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
        boundingBox = new BoundingBox(Vector.mutableDouble(position), Vector.immutable(1D, 1D));
    }

    @Override
    public void tick() {
        super.tick();
        tickFallTime();
    }

    private void tickFallTime() {
        boolean touchingPlayer = isTouching(room.player);
        if (fallTime == -1 && touchingPlayer) fallTime = 0;
        if (fallTime >= fallResetDuration) fallTime = -resetDuration;
        else if (fallTime != -1) fallTime ++;
    }

    @Override
    public boolean isSolid(Entity entity) {
        return fallTime < fallSolidDuration;
    }

    @Override
    protected void updateBoundingBox() {
        boundingBox.position.x = position.x;
        boundingBox.position.y = position.y;
    }

    @Override
    protected String getTexturePath() {
        return "falling_platform";
    }

    @Override
    protected double getTextureDeltaXFactor() {
        return 0;
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
        } else if (fallTime >= 20) {

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

    @Override
    public void reset() {
        super.reset();
        fallTime = -1;
    }
}
