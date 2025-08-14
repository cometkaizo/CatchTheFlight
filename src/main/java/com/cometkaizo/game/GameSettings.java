package com.cometkaizo.game;

import com.cometkaizo.input.InputBinding;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.registry.Registry;

import java.lang.reflect.Field;

public class GameSettings {

    public Registry<InputBinding> inputBindings = InputBindings.INPUT_BINDINGS;
    public final int originalTileSize = 16;
    public final int renderScale = 4;
    public final int tileSize = originalTileSize * renderScale;
    public final double widthInTiles = 20;
    public final double heightInTiles = 11.25;


    public String toString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(getClass().getSimpleName());
            builder.append('{');

            for (Field field : getClass().getFields()) {
                builder.append("\n\t")
                        .append(field.getName())
                        .append(": ")
                        .append(field.get(this));
            }

            if (getClass().getFields().length > 0)
                builder.append('\n');
            builder.append('}');
            return builder.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
