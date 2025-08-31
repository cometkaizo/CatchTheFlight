package com.cometkaizo.screen;

import com.cometkaizo.Main;
import com.cometkaizo.util.ImageUtils;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    private static final Map<String, Image> TEXTURES = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Font> FONTS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Sound> SOUNDS = Collections.synchronizedMap(new HashMap<>());
    public static Image texture(String path) {
        return TEXTURES.computeIfAbsent("/assets/" + path + ".png", p -> ImageUtils.readImage(p));
    }
    public static Font font(String path) {
        return FONTS.computeIfAbsent("/assets/gui/font/" + path + ".ttf", p -> {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, Main.getResource(p));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static Sound sound(String path) {
        return SOUNDS.computeIfAbsent("/assets/sound/" + path + ".wav", p -> {
            try (var in = new BufferedInputStream(Main.getResource(p))) {
                return new Sound(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
