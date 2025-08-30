package com.cometkaizo;

import com.cometkaizo.app.GameDriver;

// Sound X
// Dialogue X
// Collectibles X
// Game end X
// Secrets
//     Map Room X (make the floor a map and change the collectible to a pin)
//     Bathroom -> golden toilet paper roll
//     Bamboo cafe / gift shop -> coffee
//     Restaurant -> soda or croissant
// Background art instead of just random shapes
// Art (the cozy bamboo cafe thing)
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