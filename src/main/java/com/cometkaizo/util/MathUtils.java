package com.cometkaizo.util;

public class MathUtils {
    public static double lerp(double a, double from, double to) {
        return from + (a * (to - from));
    }

    public static boolean almostEquals(double a, double b) {
        return Math.abs(b - a) < 1.0E-5D;
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

    public static boolean isBetween(double n, double a, double b) {
        if (a < b) return (a - 1E-5) < n && n < (b + 1E-5);
        else return (b - 1E-5) < n && n < (a + 1E-5);
    }
}
