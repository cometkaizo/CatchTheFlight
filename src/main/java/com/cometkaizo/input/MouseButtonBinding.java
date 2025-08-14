package com.cometkaizo.input;

public class MouseButtonBinding implements InputBinding {
    public final String name;
    public int button;
    public final int defaultKey;
    public boolean isDown;

    public MouseButtonBinding(String name, int defaultKey, int button) {
        this.name = name;
        this.button = button;
        this.defaultKey = defaultKey;
    }
    public MouseButtonBinding(String name, int defaultKey) {
        this(name, defaultKey, defaultKey);
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public void resetKey() {
        this.button = this.defaultKey;
    }

    public int getDefaultKey() {
        return defaultKey;
    }

    @Override
    public boolean isActive() {
        return isDown;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    @Override
    public String getName() {
        return name;
    }
}
