package com.cometkaizo.screen;

import com.cometkaizo.util.MathUtils;

import java.awt.*;

public class Canvas {
    private int screenWidth, screenHeight;
    private double cameraX, cameraY;
    private double oldCameraX, oldCameraY;
    private double coordToScreen;
    private double renderScale;
    private Graphics2D g;
    private double partialTick;

    public Canvas(double coordToScreen, double cameraX, double cameraY, double renderScale, Graphics2D g) {
        this.coordToScreen = coordToScreen;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.renderScale = renderScale;
        this.g = g;
    }
    public Canvas() {

    }

    public void render(Shape shape) {
        g.draw(shape);
    }

    public void renderImage(Image image, double x, double y) {
        renderImage(image, x, y, 0, 0);
    }

    public void renderImage(Image image, double x, double y, double deltaXFactor, double deltaYFactor) {
        renderImage(image, toScreenX(x), toScreenY(y), deltaXFactor, deltaYFactor);
    }

    public void renderImage(Image image, int x, int y) {
        renderImage(image, x, y, 0, 0);
    }

    public void renderImage(Image image, int x, int y, double deltaXFactor, double deltaYFactor) {
        double width = image.getWidth(null) * renderScale;
        double height = image.getHeight(null) * renderScale;

        double actualX = x + width * deltaXFactor;
        double actualY = y + height * deltaYFactor;

        if (isNotVisible(actualX, actualY, width, height)) return;

        g.drawImage(image,
                (int) actualX,
                (int) actualY,
                (int) width,
                (int) height,
                null);
    }

    private boolean isNotVisible(double x, double y, double width, double height) {
        return x >= screenWidth || y >= screenHeight ||
                x + width <= 0 || y + height <= 0;
    }

    public int toScreenX(double coordX) {
        return toScreenX(coordX, getCameraX());
    }

    public int toScreenY(double coordY) {
        return toScreenY(coordY, getCameraY());
    }

    public int toScreenX(double coordX, double originX) {
        return (int) ((coordX - originX) * coordToScreen + screenWidth / 2D);
    }

    public int toScreenY(double coordY, double originY) {
        return screenHeight / 2 - (int) ((coordY - originY) * coordToScreen);
    }

    public int toScreenLength(double coordLen) {
        return (int) (coordLen * coordToScreen);
    }

    public Graphics2D getGraphics() {
        return g;
    }

    public int getWidth() {
        return screenWidth;
    }

    public int getHeight() {
        return screenHeight;
    }

    void startRender(Graphics2D g, double oldCameraX, double oldCameraY, double cameraX, double cameraY, int width, int height, double partialTick) {
        this.g = g;
        this.oldCameraX = oldCameraX;
        this.oldCameraY = oldCameraY;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.screenWidth = width;
        this.screenHeight = height;
        this.partialTick = partialTick;
    }

    public double getCameraX() {
        return lerp(oldCameraX, cameraX);
    }
    public double getCameraY() {
        return lerp(oldCameraY, cameraY);
    }

    void endRender() {
        this.g = null;
    }

    public double lerp(double from, double to) {
        return MathUtils.lerp(partialTick, from, to);
    }

    public double partialTick() {
        return partialTick;
    }
}
