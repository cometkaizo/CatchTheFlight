package com.cometkaizo.screen;

import com.cometkaizo.util.StringUtils;
import com.cometkaizo.world.Tickable;

import java.awt.*;
import java.util.List;

public class Dialogue implements Tickable, Renderable {
    public static final Image TEXTURE = Assets.texture("gui/dialogue");
    private static final Font FONT = Assets.font("BoldPixels").deriveFont(Font.PLAIN, 36);
    public List<String> lines;
    public final String message;
    public final Image image;
    public final Dialogue next;
    private int length;
    private int shownLength;

    public Dialogue(String message, String image, Dialogue next) {
        this.message = message;
        this.image = Assets.texture(image);
        this.next = next;
    }

    @Override
    public void render(Canvas canvas) {
        var g = canvas.getGraphics();
        if (lines == null) {
            lines = StringUtils.createLines(message, g.getFontMetrics(), (int) (43 * canvas.renderScale()));
            length = lines.stream().map(String::length).reduce(Integer::sum).orElse(0);
        }

        var oF = g.getFont();
        g.setFont(FONT);

        int midX = canvas.getWidth() / 2;
        int top = canvas.getHeight() - canvas.toScreenLength(4.5);

        canvas.renderImage(TEXTURE, midX, top, -0.5, 0);
        canvas.renderImage(image, (int) (midX + 40 * canvas.renderScale()), (int) (top + 8 * canvas.renderScale()));

        int charsLeft = shownLength, lineId = 0;
        while (charsLeft > 0 && lineId < lines.size()) {
            String line = lines.get(lineId);

            g.drawString(line.substring(0, Math.min(line.length(), charsLeft)), (int) (midX - 88 * canvas.renderScale()), (int) (top + 14 * canvas.renderScale()) + 36 * lineId);

            charsLeft -= line.length();
            lineId ++;
        }

        g.setFont(oF);
    }

    @Override
    public void tick() {
        if (shownLength < length) {
            tickSounds();
            shownLength++;
        }
    }

    private void tickSounds() {
        if (shownLength % 2 == 0) Assets.sound("select").play();
    }

    public boolean isFinished() {
        return shownLength == length;
    }
    public void finish() {
        shownLength = length;
    }
}
