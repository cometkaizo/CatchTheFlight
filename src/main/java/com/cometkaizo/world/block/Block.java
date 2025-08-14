package com.cometkaizo.world.block;

import com.cometkaizo.world.*;
import com.cometkaizo.io.DataSerializable;
import com.cometkaizo.io.data.CompoundData;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;

import java.awt.*;

public abstract class Block implements Tickable, Renderable, DataSerializable, Resettable {
    public static final String TYPE_KEY = "type";
    public static final String POSITION_KEY = "position";
    public final Room room;
    protected final Args originalArgs;
    public String name;
    public final Vector.ImmutableInt position;
    private Image texture = getTexture();

    public Block(Room room, Vector.ImmutableInt position, Args args) {
        this.room = room;
        this.position = position;
        this.originalArgs = args;
        reset();
    }

    @Override
    public void reset() {
        originalArgs.reset();
        this.name = originalArgs.next();
    }

    public abstract boolean isSolid();

    @Override
    public void render(Canvas canvas) {
        if (texture == null) return;
        canvas.renderImage(texture, (double) getX(), getY(), 0, -1);
    }
    protected abstract String getTexturePath();
    private Image getTexture() {
        if (getTexturePath() == null) return null;
        return Assets.texture("blocks/" + getTexturePath());
    }

    public String getNamespace() {
        return "";
    }

    public Vector.ImmutableInt getPosition() {
        return position;
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }
    public String getName() {
        return name;
    }

    public interface Reader {
        Block apply(Room room, Vector.ImmutableInt pos, Args args);
    }

    @Override
    public CompoundData write() {
        return null;
    }

    @Override
    public void read(CompoundData data) {

    }
}
