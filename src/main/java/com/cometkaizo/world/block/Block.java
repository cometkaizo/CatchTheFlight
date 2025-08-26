package com.cometkaizo.world.block;

import com.cometkaizo.world.*;
import com.cometkaizo.io.DataSerializable;
import com.cometkaizo.io.data.CompoundData;
import com.cometkaizo.screen.Assets;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;
import com.cometkaizo.world.entity.Entity;

import java.awt.*;

public abstract class Block implements Tickable, Renderable, DataSerializable, Resettable {
    public static final String TYPE_KEY = "type";
    public static final String POSITION_KEY = "position";
    public final Room room;
    public final Room.Layer layer;
    protected final Args originalArgs;
    public String name;
    public final Vector.ImmutableInt position;

    public Block(Room.Layer layer, Vector.ImmutableInt position, Args args) {
        this.room = layer.room;
        this.layer = layer;
        this.position = position;
        this.originalArgs = args;
        reset();
    }

    @Override
    public void reset() {
        originalArgs.reset();
        this.name = originalArgs.next();
    }

    public abstract boolean isSolid(Entity entity);

    @Override
    public void render(Canvas canvas) {
        var texture = getTexture();
        if (texture == null) return;
        canvas.renderImage(texture, (double) getX(), getY(), getTextureDeltaXFactor(), getTextureDeltaYFactor());
    }
    protected double getTextureDeltaXFactor() {
        return 0;
    }
    protected double getTextureDeltaYFactor() {
        return -1;
    }
    protected abstract String getTexturePath();
    private Image getTexture() {
        var texturePath = getTexturePath();
        if (texturePath == null) return null;
        return Assets.texture("block/" + texturePath);
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
        Block apply(Room.Layer layer, Vector.ImmutableInt pos, Args args);
    }

    @Override
    public CompoundData write() {
        return null;
    }

    @Override
    public void read(CompoundData data) {

    }
}
