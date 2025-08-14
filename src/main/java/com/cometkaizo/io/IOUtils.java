package com.cometkaizo.io;

public class IOUtils {

    public static String toNamespace(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

}
