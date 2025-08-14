package com.cometkaizo.io.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleData implements Data {
    public static final Type TYPE = new Type();
    private double value;
    private boolean isDirty = true;

    public DoubleData(double value) {
        this.value = value;
    }

    public DoubleData(DataInput input) throws IOException {
        read(input);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (this.value == value) return;
        this.value = value;
        isDirty = true;
    }

    @Override
    public void read(DataInput input) throws IOException {
        value = input.readDouble();
        isDirty = false;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(getType().getNamespace());
        output.writeDouble(value);
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


    public static class Type implements Data.Type<DoubleData> {
        @Override
        public DoubleData read(DataInput input) throws IOException {
            return new DoubleData(input);
        }
    }
}
