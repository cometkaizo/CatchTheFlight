package com.cometkaizo.io.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DataEnd implements Data {
    public static final Type<DataEnd> TYPE = input -> getInstance();

    private DataEnd() {
    }

    public static void endData(DataOutput output) throws IOException {
        getInstance().write(output);
    }

    @Override
    public void read(DataInput input) throws IOException {

    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(TYPE.getNamespace());
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public Type<DataEnd> getType() {
        return TYPE;
    }

    public static DataEnd getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        static final DataEnd INSTANCE = new DataEnd();
    }
}
