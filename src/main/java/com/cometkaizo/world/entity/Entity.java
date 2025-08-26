package com.cometkaizo.world.entity;

import com.cometkaizo.world.*;
import com.cometkaizo.game.Game;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;

import java.awt.*;

public abstract class Entity implements Tickable, Renderable, Resettable {
    protected final Args originalArgs;
    protected Vector.ImmutableDouble originalPosition;
    protected Vector.MutableDouble position;
    protected Vector.ImmutableDouble oldPosition;
    protected boolean removed;
    protected final Game game;
    protected Room.Layer layer;
    protected Room room;
    protected String name;

    public Entity(Room.Layer layer, Vector.MutableDouble position, Args args) {
        this.room = layer.room;
        this.layer = layer;
        this.game = room.game;
        this.originalArgs = args;
        this.originalPosition = Vector.immutableDouble(position);
        reset();
    }

    @Override
    public void reset() {
        originalArgs.reset();
        this.position = Vector.mutableDouble(originalPosition);
        updateOldPosition();
        this.name = originalArgs.next();
    }

    @Override
    public void tick() {
        updateOldPosition();
    }

    protected void updateOldPosition() {
        this.oldPosition = Vector.immutableDouble(position);
    }

    @Override
    public void render(Canvas canvas) {
        var texture = getTexture();
        if (texture == null) return;
        canvas.renderImage(texture, canvas.lerp(oldPosition.x, getX()), canvas.lerp(oldPosition.y, getY()), getTextureDeltaXFactor(), getTextureDeltaYFactor());
    }
    protected double getTextureDeltaXFactor() {
        return -0.5;
    }
    protected double getTextureDeltaYFactor() {
        return -1;
    }
    protected abstract String getTexturePath();
    private Image getTexture() {
        String texturePath = getTexturePath();
        if (texturePath == null) return null;
        return Assets.texture("entity/" + texturePath);
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }
    public String getName() {
        return name;
    }



    public void onAddedTo(Room room) {
        this.room = room;
        removed = false;
    }

    public void onRemoved() {
        room = null;
        removed = true;
    }





    public Vector.Double getPosition() {
        return position;
    }

    public void setPosition(Vector.Double position) {
        setPosition(position.getX(), position.getY());
    }

    public void setPosition(double x, double y) {
        position.setX(x);
        position.setY(y);
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public double getOldX() {
        return oldPosition.x;
    }

    public double getOldY() {
        return oldPosition.y;
    }

    public boolean isRemoved() {
        return removed;
    }

    public interface Reader {
        Entity apply(Room.Layer layer, Vector.MutableDouble pos, Args args);
    }
}
