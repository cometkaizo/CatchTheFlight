package com.cometkaizo.world;

public enum Axis {
    X,
    Y;

    public Axis invert() {
        return this == X ? Y : X;
    }
}
