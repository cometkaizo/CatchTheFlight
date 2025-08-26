package com.cometkaizo.screen;

import com.cometkaizo.util.ImageUtils;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    private static final Map<String, Image> TEXTURES = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Font> FONTS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Sound> SOUNDS = Collections.synchronizedMap(new HashMap<>());
    public static Image texture(String path) {
        return TEXTURES.computeIfAbsent("src/main/resources/assets/" + path + ".png", p -> ImageUtils.readImage(new File(p)));
    }
    public static Font font(String path) {
        return FONTS.computeIfAbsent("src/main/resources/assets/gui/font/" + path + ".ttf", p -> {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, new File(p));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static Sound sound(String path) {
        return SOUNDS.computeIfAbsent("src/main/resources/assets/sound/" + path + ".wav", p -> {
            try (var in = new BufferedInputStream(Files.newInputStream(Path.of(p)))) {
                return new Sound(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
