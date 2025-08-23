package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

import static com.cometkaizo.util.MathUtils.almostEquals;

// todo: maybe make luggage push player away strongly
//       STRAT: MAKE LUGGAGE SOLID ONCE LEFT FROM PLAYER'S HITBOX
//       maybe (but not necessary) make player be able to jump only once luggage has landed
//       REMOVE
public class Luggage extends ThrowableEntity {
    protected int deathTime = -1;
    protected double airFriction = 0.5;

    public Luggage(Room room, Vector.MutableDouble position, Args args) {
        super(room, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
    }

    private void onKeyPressed(KeyPressedEvent event) {
        if (event.input() == InputBindings.INTERACT.get() && isTouching(room.player, 0.1)) {
            room.player.setHeld(this);
        }
    }

    public void kill() {
        room.player.killDueToLostItem();
    }

    @Override
    public void tick() {
        updateMotion();
//        alignIfLanded();
        super.tick();
        tickDeathTime();
    }

    private void alignIfLanded() { // todo: make this smoother
        if (isLanded()) {
            motion.set(0D, 0D);
            if (!aligned()) {
                setPosition(Math.round(position.x - 0.5) + 0.5, Math.round(position.y));
            }
        }
    }

    private boolean aligned() {
        return almostEquals(position.x, Math.round(position.x)) && almostEquals(position.y, Math.round(position.y));
    }

    private boolean isLanded() {
        return !isHeld() && motion.isShorterThan(0.01);
    }

    private void updateMotion() {
        motion.x *= airFriction;
        motion.y *= airFriction;
    }

    private void tickDeathTime() {
        if (isFloating()) {
            deathTime ++;
            if (deathTime == 20) kill();
        } else deathTime = -1;
    }

    private boolean isFloating() {
        return !isHeld() && !room.ground.containsSolid(boundingBox, this);
    }

    @Override
    public void reset() {
        super.reset();
        deathTime = -1;
    }

    /*@Override
    public boolean isSolid(Entity entity) {
        return isLanded();
    }*/

    @Override
    protected void updateBoundingBox() {
        double width = boundingBox.getWidth();
        boundingBox.position.x = position.x - width / 2;
        boundingBox.position.y = position.y;
    }

    @Override
    protected String getTexturePath() {
        return "luggage";
    }

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();

        if (deathTime >= 10) {// todo: make fade out or get smaller (player too)
            g.rotate(Math.toRadians((deathTime - 10 + canvas.partialTick()) * 40), canvas.toScreenX(canvas.lerp(getOldX(), getX())), canvas.toScreenY(canvas.lerp(getOldY(), getY()) + 0.5));
        } else if (deathTime >= 0) {
            g.rotate(Math.sin((deathTime + canvas.partialTick()) * 1.3) * 0.2, canvas.toScreenX(canvas.lerp(getOldX(), getX())), canvas.toScreenY(canvas.lerp(getOldY(), getY()) + 0.5));
        }
        super.render(canvas);

        g.setTransform(oT);
    }
}
