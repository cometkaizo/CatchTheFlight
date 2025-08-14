package com.cometkaizo.input;

public class KeyBinding implements InputBinding {
    public final String name;
    public int key;
    public final int defaultKey;
    public boolean isDown;

    public KeyBinding(String name, int defaultKey, int key) {
        this.name = name;
        this.key = key;
        this.defaultKey = defaultKey;
    }
    public KeyBinding(String name, int defaultKey) {
        this(name, defaultKey, defaultKey);
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void resetKey() {
        this.key = this.defaultKey;
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
