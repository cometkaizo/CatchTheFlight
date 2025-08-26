package com.cometkaizo.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static List<String> createLines(String text, FontMetrics font, int width) {
        String[] words = text.split(" ");

        List<String> lines = new ArrayList<>();
        lines.add("");

        for (String word : words) {
            String line = lines.getLast();
            String extendedLine = line.isEmpty() ? word : line + " " + word;
            if (font.stringWidth(extendedLine) > width) { // move word to next line
                lines.add(word);
            } else {
                lines.set(lines.size() - 1, extendedLine);
            }
        }

        return lines;
    }

}
