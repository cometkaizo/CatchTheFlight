package com.cometkaizo.util;

public class MathUtils {
    public static double lerp(double a, double from, double to) {
        return from + (a * (to - from));
    }

    public static boolean almostEquals(double a, double b) {
        return Math.abs(b - a) < (double)1.0E-5F;
    }

    public static double clamp(double a, double lo, double hi) {
        return Math.max(lo, Math.min(a, hi));
    }

    public static int getSheetCol(String str) {
        str = str.toUpperCase();
        int col = 0;
        for (int i = 0; i < str.length(); i ++) {
            col *= 26;
            col += str.charAt(i) - 'A' + 1;
        }
        return col - 1; // make it 0-indexed
    }
}
