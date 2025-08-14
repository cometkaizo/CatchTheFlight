package com.cometkaizo;

import com.cometkaizo.app.GameDriver;

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

    public static void log(String message) {
        System.out.println(message);
    }
    public static void err(String message) {
        System.err.println(message);
    }
}