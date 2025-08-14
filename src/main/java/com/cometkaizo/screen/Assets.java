package com.cometkaizo.screen;

import com.cometkaizo.util.ImageUtils;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Assets {
    private static final Map<String, Image> TEXTURES = new HashMap<>();
    public static Image texture(String path) {
        return TEXTURES.computeIfAbsent("src/main/resources/assets/" + path + ".png", p -> ImageUtils.readImage(new File(p)));
    }
}
