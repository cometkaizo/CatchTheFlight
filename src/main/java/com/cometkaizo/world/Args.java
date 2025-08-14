package com.cometkaizo.world;

public class Args {
    public String[] args;
    public int index;
    public Args(String s) {
        args = s.split(";");
    }
    public String next() {
        if (index == args.length) return "";
        return args[index ++];
    }
    public String next(String defaultVal) {
        var next = next();
        if (next.isBlank()) return defaultVal;
        else return next;
    }

    public void reset() {
        index = 0;
    }
}
