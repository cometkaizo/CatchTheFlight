package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.util.MathUtils;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

import java.awt.*;

import static com.cometkaizo.util.MathUtils.almostEquals;

// todo: collision does not account for if solid entity such as luggage moves into player
public class Luggage extends ThrowableEntity {
    protected final int alignDuration = 5;
    protected int deathTime = -1, alignTime = -1;
    protected double airFriction = 0.5;
    protected Vector.ImmutableDouble unalignedPos;
    protected boolean solid;

    public Luggage(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position, args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
    }

    private void onKeyPressed(KeyPressedEvent event) {
        if (event.input() == InputBindings.INTERACT.get() && canBeInteracted()) {
            room.player.setHeld(this);
            room.player.onInteract();
            Assets.sound("hold").play();
            alignTime = -1;
            unalignedPos = null;
            solid = false;
        }
    }

    private boolean canBeInteracted() {
        return isTouching(room.player, 0.1) && room.player.canInteract() && !room.player.isFloating();
    }

    public void kill() {
        room.player.killUnrecoverable();
    }

    @Override
    public void tick() {
        updateMotion();
        tickAlignment();
        super.tick();
        tickButtonPress();
        tickDeathTime();
    }

    private void tickAlignment() {
        if (isLanded()) {
            if (groundMotion.isZero()) {
                motion.set(0D, 0D);
                if (!isTouching(room.player)) solid = true;
                expandBoundingBox();
                if (!aligned()) {
                    if (alignTime == -1) {
                        unalignedPos = Vector.immutableDouble(position);
                        alignTime = 0;
                    }
                    setPosition(Math.round(position.x - 0.5) + 0.5, Math.round(position.y));
                }
            }
        } else {
            shrinkBoundingBox();
        }
        if (alignTime >= alignDuration) {
            unalignedPos = null;
            alignTime = -1;
        } else if (alignTime > -1) alignTime ++;
    }

    private void tickButtonPress() {
        if (isLanded() && groundMotion.isZero()) {
            for (var e : layer.entities) {
                if (e instanceof Button b && isTouching(b, -0.1)) b.holdDownByEntity();
            }
        }
    }

    private boolean aligned() {
        return almostEquals(position.x, Math.floor(position.x) + 0.5) && almostEquals(position.y, Math.round(position.y));
    }

    private void shrinkBoundingBox() {
        if (boundingBox.size.x == 1) {
//            boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(0.6D, 0.6D));
            tickBoundingBox();
        }
    }
    private void expandBoundingBox() {
        if (boundingBox.size.x != 1) {
            boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
            tickBoundingBox();
        }
    }

    private boolean isLanded() {
        return !isHeld() && motion.isShorterThan(0.03);
    }

    private void updateMotion() {
        motion.x *= airFriction;
        motion.y *= airFriction;
    }

    private void tickDeathTime() {
        if (isFloating() && isLanded() && aligned()) {
            deathTime ++;
            if (deathTime == 5) Assets.sound("fall2").play();
            if (deathTime == 20) kill();
        } else deathTime = -1;
    }

    public boolean isFloating() {
        return !isHeld() && super.isFloating();
    }

    @Override
    public void reset() {
        super.reset();
        deathTime = -1;
        alignTime = -1;
        unalignedPos = null;
        solid = false;
    }

    @Override
    public boolean canBeMovedBy(Entity other) {
        return !isLanded() || // can be moved if not landed & aligned
                directlyAbove(other); // or if it is landed, can only be moved if the mover is directly below it
    }

    private boolean directlyAbove(Entity other) {
        if (!(other instanceof CollidableEntity c)) return false;
        return c.boundingBox.position.addedTo(c.boundingBox.getWidth() / 2, 0D).almostEquals(position);
    }

    @Override
    public boolean isSolid(Entity entity) {
        return solid;
    }

    @Override
    protected void tickBoundingBox() {
        double width = boundingBox.getWidth();
        boundingBox.position.x = position.x - width / 2;
        boundingBox.position.y = position.y;
    }

    @Override
    protected String getTexturePath() {
        return "luggage/" + (canBeInteracted() ? "hovered" : "normal");
    }

    @Override
    public void render(Canvas canvas) {
        if (isHeld()) return;

        var g = canvas.getGraphics();
        var oT = g.getTransform();
        var oC = g.getComposite();

        int screenX = canvas.toScreenX(canvas.lerp(getOldX(), getX()));
        int screenY = canvas.toScreenY(canvas.lerp(getOldY(), getY()) + 0.5);

        double translateX = 0, translateY = 0;
        double alpha = 1;

        int deathRecoveryDuration = 5;
        if (alignTime > -1) {
            int alignTransitionX = canvas.toScreenX(MathUtils.lerp(Math.min(1, (alignTime + canvas.partialTick()) / alignDuration), unalignedPos.x, position.x));
            int alignTransitionY = canvas.toScreenY(MathUtils.lerp(Math.min(1, (alignTime + canvas.partialTick()) / alignDuration), unalignedPos.y, position.y) + 0.5);
            translateX = alignTransitionX - screenX;
            translateY = alignTransitionY - screenY;
        } else if (deathTime >= deathRecoveryDuration) {
            translateY = Math.pow((deathTime + canvas.partialTick() - deathRecoveryDuration) * 0.2, 2.5) * 25;
            alpha = Math.pow(1 - (deathTime + canvas.partialTick() - deathRecoveryDuration) / (20 - deathRecoveryDuration) * 1, 5);
        }
        alpha = Math.clamp(alpha, 0, 1);

        {
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
