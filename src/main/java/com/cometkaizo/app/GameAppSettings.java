package com.cometkaizo.app;

import com.cometkaizo.io.ResourceLocation;
import com.cometkaizo.screen.GameRenderer;
import com.cometkaizo.system.app.AppSettings;

import java.awt.*;
import java.nio.file.Path;

public class GameAppSettings extends AppSettings {

    public int defaultWidth = 1280;
    public int defaultHeight = 720;
    public String name = "Unnamed game";
    public Color defaultBackgroundColor = new Color(255, 237, 212);
    public GameRenderer.Settings defaultRendererSettings = new GameRenderer.Settings(new Dimension(defaultWidth, defaultHeight), defaultBackgroundColor);

    public Path gamePath = Path.of("E:/andyw/Data/.game");
    public final String gameSaveSubPath = "saves";
    public ResourceLocation resourceWorldsPath = new ResourceLocation("data/worlds");
    public String newWorldSubPath = "original";
}
