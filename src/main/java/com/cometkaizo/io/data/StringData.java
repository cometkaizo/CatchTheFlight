package com.cometkaizo.io.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StringData implements Data {
    public static final Type TYPE = new Type();
    private String value;
    private boolean isDirty = true;

    public StringData(String value) {
        this.value = value;
    }

    public StringData(DataInput input) throws IOException {
        read(input);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (Objects.equals(this.value, value)) return;
        this.value = value;
        isDirty = true;
    }

    @Override
    public void read(DataInput input) throws IOException {
        value = input.readUTF();
        isDirty = false;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(getType().getNamespace());
        output.writeUTF(value);
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


    public static class Type implements Data.Type<StringData> {
        @Override
        public StringData read(DataInput input) throws IOException {
            return new StringData(input);
        }
    }
}
