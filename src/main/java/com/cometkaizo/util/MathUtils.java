package com.cometkaizo.util;

public class MathUtils {
    public static double lerp(double a, double from, double to) {
        return from + (a * (to - from));
    }

    public static boolean almostEquals(double a, double b) {
        return Math.abs(b - a) < (double)1.0E-5F;
    }

    public static double clamp(double a, int lo, int hi) {
        return Math.max(lo, Math.min(a, hi));
    }
}
