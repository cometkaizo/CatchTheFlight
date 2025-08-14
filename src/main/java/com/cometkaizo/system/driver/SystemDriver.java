package com.cometkaizo.system.driver;

import com.cometkaizo.system.app.App;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public abstract class SystemDriver {

    private final App app;


    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final List<Supplier<ScheduledFuture<?>>> waitingTasks = new ArrayList<>(1);
    private final List<ScheduledFuture<?>> tasks = new ArrayList<>(1);
    private boolean isRunning = false;

    protected SystemDriver(App app) {
        this.app = app;
    }

    public static InputStream getConsoleIn() {
        return System.in;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;

        setup();
        waitingTasks.forEach(task -> tasks.add(task.get()));
        waitingTasks.clear();
    }

    public void stop() {
        if (!isRunning) return;
        isRunning = false;

        cleanup();
        tasks.forEach(loop -> loop.cancel(false));
        tasks.clear();
    }

    protected void setup() {
        app.setup();
    }

    protected void cleanup() {
        app.cleanup();
    }


    protected final void addLoop(Runnable task, long period, TimeUnit unit, ExceptionManager exceptionManager) {
        addTask(() -> executor.scheduleAtFixedRate(new LoopTask(task, exceptionManager), 0, period, unit));
    }

    private record LoopTask(Runnable task, ExceptionManager exceptionManager) implements Runnable {

        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception e) {
                Throwable newEx = exceptionManager.handleException(e);
                if (newEx != null) throw newEx instanceof RuntimeException r ? r : new RuntimeException(newEx);
            } catch (Error err) {
                Throwable newEx = exceptionManager.handleError(err);
                if (newEx != null) throw newEx instanceof RuntimeException r ? r : new RuntimeException(newEx);
                throw err;
            }
        }
    }

    protected final void addLoop(Runnable task, long period, TimeUnit unit) {
        addLoop(task, period, unit, new ExceptionManager() {
            @Override
            public Throwable handleException(Exception e) {
                System.err.println("Encountered exception");
                e.printStackTrace();
                return null;
            }

            @Override
            public Error handleError(Error err) {
                System.err.println("Encountered fatal exception");
                err.printStackTrace();
                return err;
            }
        });
    }

    protected final void addTask(Supplier<ScheduledFuture<?>> task) {
        waitingTasks.add(task);
    }

    public App getApp() {
        return app;
    }

    public boolean isRunning() {
        return isRunning;
    }

}
