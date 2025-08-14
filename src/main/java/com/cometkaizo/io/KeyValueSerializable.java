package com.cometkaizo.io;

import java.io.IOException;

@SuppressWarnings("unused")
public interface KeyValueSerializable {

    void writeKV(KeyValueWriter writer) throws IOException;
    void readKV(KeyValueReader reader) throws IOException;

}
