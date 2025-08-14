package com.cometkaizo.screen;

import com.cometkaizo.app.GameDriver;
import com.cometkaizo.game.Game;
import com.cometkaizo.game.GameSettings;

import javax.swing.*;
import java.awt.*;

public class GameRenderer extends JPanel {

    private final Canvas canvas;
    private final Game game;
    private final Dimension size;
    private int rendersSinceLastTick = 0;
    private long lastTick;

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

    protected void render(Graphics2D g) {
        if (game.tick == lastTick) rendersSinceLastTick ++;
        else rendersSinceLastTick = 0;

        Dimension size = getSize(this.size);

        game.getCameraPosition().setX(game.getPlayer().getX());
        game.getCameraPosition().setY(game.getPlayer().getY());

        double partialTick = (double) rendersSinceLastTick / GameDriver.RENDERS_PER_TICK;
        canvas.startRender(g, game.getCameraPosition().x, game.getCameraPosition().y, game.getCameraPosition().x, game.getCameraPosition().y, size.width, size.height, partialTick);

        game.render(canvas);

        canvas.endRender();
        lastTick = game.tick;
    }

    public record Settings(
            Dimension size,
            Color backgroundColor
    ) {}
}
