package com.cometkaizo.io;

import java.io.File;

public class ResourceLocation extends File {
    public static final String PREFIX = "src/main/resources/";

    public ResourceLocation(String pathname) {
        super(PREFIX + pathname);
    }
    public ResourceLocation(ResourceLocation parent, String child) {
        super(parent.toString(), child);
    }
}
