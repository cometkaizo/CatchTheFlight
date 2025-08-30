package com.cometkaizo.world.entity;

import com.cometkaizo.game.event.KeyPressedEvent;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

import java.awt.*;

public class Collectible extends CollidableEntity {

    public static final int LUGGAGE_VARIATION = 0;
    protected final int collectDuration = 5;
    protected int collectTime = -1;
    protected int variation;

    public Collectible(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position.add(0.5, 0D), args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
        game.getEventBus().register(KeyPressedEvent.class, this::onKeyPressed);
    }

    private void onKeyPressed(KeyPressedEvent event) {
        if (event.input() == InputBindings.INTERACT.get() && canBeInteracted()) {
            collect();
        }
    }

    private void collect() {
        room.player.displayedCollectible = this;
        room.player.onInteract();
        collectTime = 0;
        game.collect(this);
        Assets.sound("notify").play();
    }

    private boolean canBeInteracted() {
        return isTouching(room.player) && room.player.canInteract() && !collected();
    }

    public boolean collected() {
        return collectTime > -1;
    }

    @Override
    public void reset() {
        super.reset();
        variation = originalArgs.nextInt(0);
    }

    @Override
    public void tick() {
        super.tick();
        if (collected() && collectTime < collectDuration) collectTime ++;
    }

    @Override
    protected void tickBoundingBox() {
        double width = boundingBox.getWidth();
        boundingBox.position.x = position.x - width / 2;
        boundingBox.position.y = position.y;
    }

    @Override
    public boolean canCollideWhenMoving() {
        return false;
    }

    @Override
    protected String getTexturePath() {
        return "collectible/" + (canBeInteracted() ? "hovered/" : "normal/") + variation;
    }

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();
        var oC = g.getComposite();

        int screenX = canvas.toScreenX(canvas.lerp(getOldX(), getX()) + 0.5);
        int screenY = canvas.toScreenY(canvas.lerp(getOldY(), getY()) + 0.5);

        double angle = 0;
        double translateX = 0, translateY = 0;
        double alpha = 1;

        double playerScreenX = canvas.toScreenX(canvas.lerp(room.player.getOldX(), room.player.getX()));
        double playerScreenY = canvas.toScreenY(canvas.lerp(room.player.getOldY(), room.player.getY()));

        if (!collected()) {
            if (variation == LUGGAGE_VARIATION) translateY = -canvas.toScreenLength(1.5);
        } else if (room.player.displayedCollectible == this) {
            translateX = playerScreenX - screenX + canvas.toScreenLength(0.63);
            translateY = playerScreenY - screenY - canvas.toScreenLength(2.2) * (1 - Math.pow(1 - Math.min(1, (collectTime + canvas.partialTick()) / collectDuration), 2.5));
        } else return;

        {
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

    public void renderOverlay(Canvas canvas) {
        var g = canvas.getGraphics();
        var oT = g.getTransform();

        int screenX = 20 + variation * 60;
        int screenY = 20;

        g.translate(screenX, screenY);
        g.scale(0.5, 0.5);
        g.translate(-screenX, -screenY);

        canvas.renderImage(getTexture(), 20 + variation * 60, 20);

        g.setTransform(oT);
    }

    public int getVariation() {
        return variation;
    }
}
