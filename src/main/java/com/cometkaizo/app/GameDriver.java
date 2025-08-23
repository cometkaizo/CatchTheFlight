package com.cometkaizo.app;

import com.cometkaizo.system.driver.SystemDriver;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class GameDriver extends SystemDriver {
    private final GameApp app;
    public static final int RENDERS_PER_TICK = 3;

    public GameDriver(InputStream input) {
        super(new GameApp());
        this.app = (GameApp) getApp();

        addLoop(new Runnable() {
            private final Scanner scanner = new Scanner(input);
            @Override
            public void run() {
                if (scanner.hasNextLine()) {
                    app.parseInput(scanner.nextLine());
                }
            }
        }, 300, TimeUnit.MILLISECONDS);

//        double tickTime = 1000 / 20D;
//        addLoop(app::tick, (long) tickTime, TimeUnit.MILLISECONDS);
//        addLoop(app::render, (long) (tickTime / RENDERS_PER_TICK), TimeUnit.MILLISECONDS);
        addLoop(new Runnable() {
            private int renderCnt = 0;
            @Override
            public void run() {
                renderCnt++;
                if (renderCnt % RENDERS_PER_TICK == 0) app.tick();
                app.render(renderCnt % RENDERS_PER_TICK / (double)RENDERS_PER_TICK);
            }
        }, 1000 / 60, TimeUnit.MILLISECONDS);
    }
}
