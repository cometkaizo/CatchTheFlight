package com.cometkaizo.io.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static com.cometkaizo.io.data.DataEnd.endData;

public class CompoundData implements Data {
    public static final Type TYPE = new Type();
    private final Map<String, Data> map;
    private final Map<String, Data> mapView;
    private boolean isDirty = true;

    public CompoundData(Map<String, Data> map) {
        this.map = map;
        this.mapView = Collections.unmodifiableMap(map);
    }

    public CompoundData() {
        this(new HashMap<>(2));
    }

    public CompoundData(DataInput input) throws IOException {
        this(new HashMap<>(0));
        read(input);
    }

    static CompoundData of(DataInput input) throws IOException {
        String strType = input.readUTF();
        Data.Type<?> type = DataTypes.DATA_TYPES.getValue(strType);
        if (type != TYPE) throw new InvalidClassException("Required: '" + TYPE.getNamespace() + "', found: '" + type.getNamespace() + "'");
        return TYPE.read(input);
    }

    public static CompoundData of(Path path) throws IOException {
        return of(new DataInputStream(Files.newInputStream(path)));
    }

    public void put(String key, Data data) {
        map.put(key, data);
        isDirty = true;
    }

    public void putBoolean(String key, boolean value) {
        if (!map.containsKey(key)) map.put(key, new BooleanData(value));
        else if (map.get(key) instanceof BooleanData data) data.setValue(value);
        else throwIncorrectValueTypeException(key, value);
        isDirty = true;
    }

    public void putInt(String key, int value) {
        if (!map.containsKey(key)) map.put(key, new IntData(value));
        else if (map.get(key) instanceof IntData data) data.setValue(value);
        else throwIncorrectValueTypeException(key, value);
        isDirty = true;
    }

    public void putDouble(String key, double value) {
        if (!map.containsKey(key)) map.put(key, new DoubleData(value));
        else if (map.get(key) instanceof DoubleData data) data.setValue(value);
        else throwIncorrectValueTypeException(key, value);
        isDirty = true;
    }

    public void putString(String key, String value) {
        if (!map.containsKey(key)) map.put(key, new StringData(value));
        else if (map.get(key) instanceof StringData data) data.setValue(value);
        else throwIncorrectValueTypeException(key, value);
        isDirty = true;
    }

    private void throwIncorrectValueTypeException(String key, Object value) {
        throw new IllegalStateException("Value '" + value + "' has incorrect type '" + value.getClass().getSimpleName() +
                "'; cannot be assigned to key of type '" + map.get(key).getType().getNamespace() + "'");
    }

    public Data get(String key) {
        Data value = map.get(key);
        if (value == null) throw new NoSuchElementException("Unknown key '" + key + "'; available entries are: \n" + map);
        return value;
    }

    public boolean getBoolean(String key) {
        return get(key, BooleanData.class, BooleanData::getValue);
    }

    public int getInt(String key) {
        return get(key, IntData.class, IntData::getValue);
    }

    public double getDouble(String key) {
        return get(key, DoubleData.class, DoubleData::getValue);
    }

    public String getString(String key) {
        return get(key, StringData.class, StringData::getValue);
    }

    public CompoundData getCompound(String key) {
        return get(key, CompoundData.class);
    }

    public ListData getList(String key) {
        return get(key, ListData.class);
    }

    private <T, R> R get(String key, Class<? extends T> expectedDataType, Function<T, R> valueFunction) {
        Data data = get(key);
        if (!expectedDataType.isAssignableFrom(data.getClass())) throw new ClassCastException("Incorrect data type for key '" + key + "'; expected " + expectedDataType + ", found " + data.getClass());
        return valueFunction.apply(expectedDataType.cast(data));
    }

    private <T> T get(String key, Class<? extends T> expectedDataType) {
        Data data = get(key);
        if (!expectedDataType.isAssignableFrom(data.getClass())) throw new ClassCastException("Incorrect data type for key '" + key + "'; expected " + expectedDataType + ", found " + data.getClass());
        return expectedDataType.cast(data);
    }

    public Map<String, Data> asMap() {
        return mapView;
    }

    @Override
    public void read(DataInput input) throws IOException {
        Map<String, Data> map = new HashMap<>(2);
        Data currentData;
        while ((currentData = Data.of(input)) != DataEnd.getInstance()) {
            String key = input.readUTF();
            addUniqueEntry(key, currentData, map);
        }
        this.map.clear();
        this.map.putAll(map);
        isDirty = false;
    }

    private static void addUniqueEntry(String key, Data currentData, Map<String, Data> map) {
        Data duplicate = map.putIfAbsent(key, currentData);
        if (duplicate != null) throw new IllegalStateException("Duplicate key '" + key + "' for values '" + duplicate + "' and '" + currentData + "'");
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(getType().getNamespace());
        for (String key : this.map.keySet()) {
            Data data = this.map.get(key);
            writeEntry(key, data, output);
        }
        endData(output);
        isDirty = false;
    }

    private static void writeEntry(String key, Data data, DataOutput output) throws IOException {
        data.write(output);
        output.writeUTF(key); // the key is stored after the value
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public Type getType() {
        return TYPE;
    }


    public static class Type implements Data.Type<CompoundData> {
        @Override
        public CompoundData read(DataInput input) throws IOException {
            return new CompoundData(input);
        }
    }
}
