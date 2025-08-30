package com.cometkaizo.screen;

import com.cometkaizo.app.GameDriver;
import com.cometkaizo.game.Game;
import com.cometkaizo.game.GameSettings;
import com.cometkaizo.util.MathUtils;

import javax.swing.*;
import java.awt.*;

public class GameRenderer extends JPanel {

    private final Canvas canvas;
    private final Game game;
    private final Dimension size;
    private double partialTick;

    public GameRenderer(Settings settings, Game game) {
        GameSettings gameSettings = game.getSettings();

        this.game = game;
        canvas = new Canvas(gameSettings.tileSize,
                game.getCameraPosition().x,
                game.getCameraPosition().y,
                gameSettings.renderScale,
                null);

        setPreferredSize(new Dimension((int) gameSettings.widthInTiles * gameSettings.tileSize,
                (int) gameSettings.heightInTiles * gameSettings.tileSize));
        setBackground(settings.backgroundColor);
        this.size = getSize();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        render((Graphics2D) g);
        g.dispose();
    }

    public void setPartialTick(double partialTick) {
        this.partialTick = partialTick;
    }

    protected void render(Graphics2D g) {
        Dimension size = getSize(this.size);

        canvas.startRender(g, game.getPrevCameraPosition(), game.getCameraPosition(), size.width, size.height, partialTick);

        game.render(canvas);

        canvas.endRender();
    }

    public record Settings(
            Dimension size,
            Color backgroundColor
    ) {}
}
