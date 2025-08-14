package com.cometkaizo.io.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntData implements Data {
    public static final Type TYPE = new Type();
    private int value;
    private boolean isDirty = true;

    public IntData(int value) {
        this.value = value;
    }

    public IntData(DataInput input) throws IOException {
        read(input);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (this.value == value) return;
        this.value = value;
        isDirty = true;
    }

    @Override
    public void read(DataInput input) throws IOException {
        value = input.readInt();
        isDirty = false;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(getType().getNamespace());
        output.writeInt(value);
        isDirty = false;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public Type getType() {
        return TYPE;
    }


    public static class Type implements Data.Type<IntData> {
        @Override
        public IntData read(DataInput input) throws IOException {
            return new IntData(input);
        }
    }
}
