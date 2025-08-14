package com.cometkaizo.io.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BooleanData implements Data {
    public static final Type TYPE = new Type();
    private boolean value;
    private boolean isDirty = true;

    public BooleanData(boolean value) {
        this.value = value;
    }

    public BooleanData(DataInput input) throws IOException {
        read(input);
    }

    @Override
    public void read(DataInput input) throws IOException {
        value = input.readBoolean();
        isDirty = false;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(getType().getNamespace());
        output.writeBoolean(value);
        isDirty = false;
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        if (this.value == value) return;
        this.value = value;
        isDirty = true;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    public static class Type implements Data.Type<BooleanData> {
        @Override
        public BooleanData read(DataInput input) throws IOException {
            return new BooleanData(input);
        }
    }
}
