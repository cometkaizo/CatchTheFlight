package com.cometkaizo.world.entity;

import com.cometkaizo.screen.Canvas;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Direction;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

// todo: make this fall instantly if both luggage and player are on it at the same time
public class MovingPlatform extends CollidableEntity {
    protected Direction direction;
    protected int moveAmt;
    protected double moveAmtLeft;
    protected double speed = 0.1;
    protected int fallTime = -1;

    public MovingPlatform(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
    }

    @Override
    public void tick() {
        super.tick();
        tickMotion();
        tickFallTime();
    }

    private void tickMotion() {
        double speed = Math.min(this.speed, moveAmtLeft);
        moveAmtLeft -= speed;

        var motion = Vector.immutable(speed * direction.x(), speed * direction.y());
        if (moveAmtLeft < 1e-5) {
            direction = direction.opposite();
            moveAmtLeft = moveAmt;
        }
        setMotion(motion);

        for (var moved : room.walls.getEntitiesWithin(boundingBox)) {
            if (moved instanceof MovableEntity movable) movable.move(motion);
        }
        if (room.player.boundingBox.intersects(boundingBox)) room.player.setGroundMotion(motion);
    }

    @Override
    public void reset() {
        super.reset();
        direction = originalArgs.nextDirection(Direction.RIGHT);
        moveAmtLeft = moveAmt = originalArgs.nextInt(1);
        if (motion != null) setMotion(0, 0);
    }

    private void tickFallTime() {
        if (fallTime >= 60) fallTime = -20;
        else if (fallTime != -1) fallTime ++;
    }

    @Override
    public boolean isSolid(Entity entity) {
        return fallTime < 30;
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
        return "moving_platform";
    }

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();

        // stable, wiggle, rotate + shrink
        int screenX = canvas.toScreenX(getX() + 0.5);
        int screenY = canvas.toScreenY(getY() + 0.5);

        if (fallTime >= 30) {
            g.translate(screenX, screenY);
            g.rotate(Math.toRadians((fallTime - 30 + canvas.partialTick()) * 40));
            double scale = Math.max(0, 60 - fallTime) / 30D;
            g.scale(scale, scale);
            g.translate(-screenX, -screenY);
        } else if (fallTime >= 0) {
            g.rotate(Math.sin((fallTime + canvas.partialTick()) * 1.3) * 0.2, screenX, screenY);
        } else if (fallTime <= -2) {
            g.translate(screenX, screenY);
            g.rotate(Math.toRadians((fallTime - 7 + canvas.partialTick()) * 42));
            double scale = Math.max(0, 7 + fallTime) / 7D;
            g.scale(scale, scale);
            g.translate(-screenX, -screenY);
        }

        super.render(canvas);

        g.setTransform(oT);
    }
}
