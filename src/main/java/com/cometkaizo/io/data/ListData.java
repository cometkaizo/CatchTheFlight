package com.cometkaizo.io.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cometkaizo.io.data.DataTypes.DATA_TYPES;

public class ListData implements Data {
    public static final Type TYPE = new Type();
    private Data.Type<?> componentType;
    private final List<Data> values;
    private final List<Data> valuesView;
    private boolean isDirty = true;

    private <T extends Data> ListData(Data.Type<T> type, List<T> values) {
        this.componentType = type;
        this.values = new ArrayList<>(values);
        this.valuesView = Collections.unmodifiableList(values);
    }

    private ListData(DataInput input) throws IOException {
        this.values = new ArrayList<>(0);
        this.valuesView = Collections.unmodifiableList(values);
        read(input);
    }

    public static ListData of() {
        return new ListData(null, new ArrayList<>(0));
    }

    public static <T extends Data> ListData of(Data.Type<T> type, List<T> values) {
        return new ListData(type, values);
    }

    public static ListData of(DataInput input) throws IOException {
        return new ListData(input);
    }


    public void add(Data value) {
        if (componentType != value.getType()) throwIncorrectValueTypeException(value);
        values.add(value);
        isDirty = true;
    }

    public void addBoolean(boolean value) {
        add(new BooleanData(value));
    }

    public void addInt(int value) {
        add(new IntData(value));
    }

    public void addDouble(double value) {
        add(new DoubleData(value));
    }

    public void addString(String value) {
        add(new StringData(value));
    }

    private void throwIncorrectValueTypeException(Object value) {
        throw new IllegalStateException("Value '" + value + "' has incorrect type '" + value.getClass().getSimpleName() +
                "'; cannot be added to list of type '" + componentType.getNamespace() + "'");
    }

    public List<Data> asList() {
        return valuesView;
    }


    @Override
    public void read(DataInput input) throws IOException {
        Data.Type<?> componentType = DATA_TYPES.getValue(input.readUTF());
        throwIfIncorrectType(this.componentType, componentType);
        int valueCount = input.readInt();
        List<Data> values = new ArrayList<>(valueCount);

        for (int index = 0; index < valueCount; index ++) {
            Data.Type<?> currentComponentType = DATA_TYPES.getValue(input.readUTF());
            throwIfIncorrectType(componentType, currentComponentType);
            values.add(componentType.read(input));
        }

        this.values.clear();
        this.values.addAll(values);
        this.componentType = componentType;
        isDirty = false;
    }

    private static void throwIfIncorrectType(Data.Type<?> expected, Data.Type<?> actual) throws InvalidObjectException {
        if (expected == null || expected == DataEnd.TYPE) return;
        if (expected != actual)
            throw new InvalidObjectException("Incorrect list component type; required: '" + expected.getNamespace() + "', found: '" + actual.getNamespace() + "'");
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(getType().getNamespace());
        output.writeUTF(componentType == null ? DataEnd.TYPE.getNamespace() : componentType.getNamespace());
        output.writeInt(values.size());

        for (Data value : values) {
            value.write(output);
        }
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


    public static class Type implements Data.Type<ListData> {
        @Override
        public ListData read(DataInput input) throws IOException {
            return ListData.of(input);
        }
    }
}
