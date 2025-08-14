package com.cometkaizo.io;

import java.io.IOException;
import java.io.Writer;

import static com.cometkaizo.io.KeyValues.*;

public class KeyValueWriter extends Writer {
    private final Writer writer;

    public KeyValueWriter(Writer writer) {
        this.writer = writer;
    }

    public void write(String key, String value) throws IOException {
        write(build(key, value));
    }


    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
