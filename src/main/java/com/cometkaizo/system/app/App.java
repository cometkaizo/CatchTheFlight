package com.cometkaizo.system.app;

public abstract class App {

    private final AppSettings settings;

    protected App(AppSettings settings) {
        this.settings = settings;
    }

    public void setup() {

    }

    public void cleanup() {

    }

    public void tick() {

    }


    public AppSettings getSettings() {
        return settings;
    }
}
