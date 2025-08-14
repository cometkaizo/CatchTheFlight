package com.cometkaizo.world.entity;

import com.cometkaizo.world.*;
import com.cometkaizo.game.Game;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;

import java.awt.*;

public abstract class Entity implements Tickable, Renderable, Resettable {
    public static final String TYPE_KEY = "type";
    public static final String POSITION_KEY = "position";
    protected final Args originalArgs;
    protected final Vector.ImmutableDouble originalPosition;
    protected Vector.MutableDouble position;
    protected Vector.ImmutableDouble oldPosition;
    protected boolean removed;
    protected final Game game;
    protected Room room;
    protected String name;
    private Image texture = getTexture();

    public Entity(Room room, Vector.MutableDouble position, Args args) {
        this.room = room;
        this.game = room.game;
        this.originalArgs = args;
        this.originalPosition = Vector.immutableDouble(position);
        reset();
    }

    @Override
    public void reset() {
        originalArgs.reset();
        this.position = Vector.mutableDouble(originalPosition);
        this.oldPosition = Vector.immutableDouble(position);
        this.name = originalArgs.next();
    }

    @Override
    public void tick() {
        this.oldPosition = Vector.immutableDouble(position);
    }

    @Override
    public void render(Canvas canvas) {
        if (texture == null) return;
        canvas.renderImage(texture, canvas.lerp(oldPosition.x, getX()), canvas.lerp(oldPosition.y, getY()), -0.5, -1);
    }
    protected abstract String getTexturePath();
    private Image getTexture() {
        if (getTexturePath() == null) return null;
        return Assets.texture("entities/" + getTexturePath());
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

    public boolean isRemoved() {
        return removed;
    }

    public interface Reader {
        Entity apply(Room room, Vector.MutableDouble pos, Args args);
    }
}
