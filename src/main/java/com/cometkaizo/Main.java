package com.cometkaizo;

import com.cometkaizo.app.GameDriver;

// Sound X
// Dialogue X
// Collectibles
// Art (the cozy bamboo cafe thing)
// Game end
public class Main {
    private static final GameDriver driver = new GameDriver(System.in);

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        driver.start();
    }
    public static void stop(int exitCode) {
        driver.stop();
        System.exit(exitCode);
    }

    // todo: depth buffer (to fix item rendering and y-ordered rendering): https://github.com/caiiiycuk/zcomposite/tree/master

    public static void log(String message) {
        System.out.println(message);
    }
    public static void err(String message) {
        System.err.println(message);
    }
}