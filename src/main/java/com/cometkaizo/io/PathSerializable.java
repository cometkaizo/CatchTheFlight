package com.cometkaizo.io;

import java.io.IOException;
import java.nio.file.Path;

public interface PathSerializable {
    /**
     * Writes this object into the specified path
     * @param path the path to save in
     * @throws IOException If an IOException occurs
     * @throws IllegalArgumentException Optionally if the specified path is the wrong file type
     */
    void write(Path path) throws IOException;
    /**
     * Reads from the specified path into this object
     * @param path the path
     * @throws IOException If an IOException occurs
     * @throws IllegalArgumentException Optionally if the specified path is the wrong file type
     */
    void read(Path path) throws IOException;
}
