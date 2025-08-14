package com.cometkaizo.io;

import java.io.*;
import java.nio.file.Files;

public interface FileSerializable {

    void write(Writer writer) throws IOException;
    void read(BufferedReader reader) throws IOException;

    default void write(File file) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(file.toPath());
        write(writer);
        writer.close();
    }

    default void read(File file) throws IOException {
        BufferedReader reader = Files.newBufferedReader(file.toPath());
        read(reader);
        reader.close();
    }
}
