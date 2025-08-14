package com.cometkaizo.io.data;

import com.cometkaizo.registry.Registry;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class DataTypes {

    public static final Registry<Data.Type<?>> DATA_TYPES = new Registry<>();

    public static final Supplier<Data.Type<?>> BOOLEAN = DATA_TYPES.register("boolean", app -> BooleanData.TYPE);
    public static final Supplier<Data.Type<?>> INT = DATA_TYPES.register("int", app -> IntData.TYPE);
    public static final Supplier<Data.Type<?>> DOUBLE = DATA_TYPES.register("double", app -> DoubleData.TYPE);
    public static final Supplier<Data.Type<?>> STRING = DATA_TYPES.register("string", app -> StringData.TYPE);
    public static final Supplier<Data.Type<?>> COMPOUND = DATA_TYPES.register("compound", app -> CompoundData.TYPE);
    public static final Supplier<Data.Type<?>> LIST = DATA_TYPES.register("list", app -> ListData.TYPE);
    public static final Supplier<Data.Type<?>> END = DATA_TYPES.register("end", app -> DataEnd.TYPE);

}
