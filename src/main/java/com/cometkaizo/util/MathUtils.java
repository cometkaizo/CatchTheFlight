package com.cometkaizo.util;

public class MathUtils {
    public static double lerp(double a, double from, double to) {
        return from + (a * (to - from));
    }
}
