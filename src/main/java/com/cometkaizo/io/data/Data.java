package com.cometkaizo.io.data;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Data {

    void read(DataInput input) throws IOException;
    void write(DataOutput output) throws IOException;
    boolean isDirty();

    Type<?> getType();

    interface Type<T extends Data> {
        T read(DataInput input) throws IOException;
        default String getNamespace() {
            return DataTypes.DATA_TYPES.getKey(this);
        }
    }

    static Data of(DataInput input) throws IOException {
        String type = input.readUTF();
        return DataTypes.DATA_TYPES.getValue(type).read(input);
    }

    static Data of(Path path) throws IOException {
        return of(new DataInputStream(Files.newInputStream(path)));
    }

}
