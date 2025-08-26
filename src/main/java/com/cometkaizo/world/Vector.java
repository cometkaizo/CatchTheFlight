package com.cometkaizo.world;

import com.cometkaizo.io.DataSerializable;
import com.cometkaizo.io.data.CompoundData;

@SuppressWarnings("unused")
public interface Vector<T extends Number> extends DataSerializable {
    String X_KEY = "x";
    String Y_KEY = "y";

    T getX();
    T getY();
    default T get(Axis axis) {
        return axis == Axis.X ? getX() : getY();
    }
    Immutable<T> addedTo(Vector<?> other);
    Immutable<T> addedTo(T x, T y);
    Immutable<T> subtractedBy(Vector<?> other);
    Immutable<T> subtractedBy(T x, T y);

    default Immutable<T> multipliedBy(Vector<?> other) {
        return multipliedBy(other.getX().doubleValue(), other.getY().doubleValue());
    }

    Immutable<T> multipliedBy(double xFactor, double yFactor);

    default Immutable<T> scaledBy(double factor) {
        return multipliedBy(factor, factor);
    }

    default boolean isShorterThan(double len) {
        return lengthSqr() < len * len;
    }
    default double lengthSqr() {
        return getX().doubleValue() * getX().doubleValue() + getY().doubleValue() * getY().doubleValue();
    }
    default double length() {
        return Math.sqrt(lengthSqr());
    }
    default double distanceSqr(Vector<?> other) {
        return subtractedBy(other).lengthSqr();
    }
    default double distance(Vector<?> other) {
        return subtractedBy(other).length();
    }
    default ImmutableDouble normalized() {
        double length = length();
        return immutable(getX().doubleValue() / length, getY().doubleValue() / length);
    }


    static ImmutableInt immutable(int x, int y) {
        return new ImmutableInt(x, y);
    }
    static ImmutableDouble immutable(double x, double y) {
        return new ImmutableDouble(x, y);
    }
    static MutableInt mutable(int x, int y) {
        return new MutableInt(x, y);
    }
    static MutableDouble mutable(double x, double y) {
        return new MutableDouble(x, y);
    }

    static ImmutableInt immutableInt(Vector<?> vector) {
        return new ImmutableInt(vector);
    }
    static ImmutableDouble immutableDouble(Vector<?> vector) {
        return new ImmutableDouble(vector);
    }
    static MutableInt mutableInt(Vector<?> vector) {
        return new MutableInt(vector);
    }
    static MutableDouble mutableDouble(Vector<?> vector) {
        return new MutableDouble(vector);
    }

    static ImmutableInt immutableInt(CompoundData data) {
        return new ImmutableInt(data);
    }
    static ImmutableDouble immutableDouble(CompoundData data) {
        return new ImmutableDouble(data);
    }
    static MutableInt mutableInt(CompoundData data) {
        return new MutableInt(data);
    }
    static MutableDouble mutableDouble(CompoundData data) {
        return new MutableDouble(data);
    }


    interface Int extends Vector<Integer> {
        @Override
        default Integer get(Axis axis) {
            return Vector.super.get(axis);
        }

        ImmutableInt addedTo(Integer x, Integer y);
        ImmutableInt subtractedBy(Integer x, Integer y);

        default ImmutableInt addedTo(Vector<?> other) {
            return addedTo(other.getX().intValue(), other.getY().intValue());
        }

        default ImmutableInt subtractedBy(Vector<?> other) {
            return subtractedBy(other.getX().intValue(), other.getY().intValue());
        }

        @Override
        default ImmutableInt multipliedBy(Vector<?> other) {
            return multipliedBy(other.getX().doubleValue(), other.getY().doubleValue());
        }

        ImmutableInt multipliedBy(double xFactor, double yFactor);

        @Override
        default ImmutableInt scaledBy(double factor) {
            return multipliedBy(factor, factor);
        }
    }
    interface Double extends Vector<java.lang.Double> {
        @Override
        default java.lang.Double get(Axis axis) {
            return Vector.super.get(axis);
        }

        ImmutableDouble addedTo(java.lang.Double x, java.lang.Double y);
        ImmutableDouble subtractedBy(java.lang.Double x, java.lang.Double y);

        default ImmutableDouble addedTo(Vector<?> other) {
            return addedTo(other.getX().doubleValue(), other.getY().doubleValue());
        }

        default ImmutableDouble subtractedBy(Vector<?> other) {
            return subtractedBy(other.getX().doubleValue(), other.getY().doubleValue());
        }

        @Override
        default ImmutableDouble multipliedBy(Vector<?> other) {
            return multipliedBy(other.getX().doubleValue(), other.getY().doubleValue());
        }

        ImmutableDouble multipliedBy(double xFactor, double yFactor);

        @Override
        default ImmutableDouble scaledBy(double factor) {
            return multipliedBy(factor, factor);
        }
    }
    interface Mutable<T extends Number> extends Vector<T> {
        void setX(T x);
        void setY(T y);
        Mutable<T> add(T x, T y);
        Mutable<T> subtract(T x, T y);
        Mutable<T> add(Vector<?> other);
        Mutable<T> subtract(Vector<?> other);
        default Mutable<T> multiply(Vector<?> other) {
            return multiply(other.getX().doubleValue(), other.getY().doubleValue());
        }
        Mutable<T> multiply(double xFactor, double yFactor);
        default Mutable<T> scale(double factor) {
            return multiply(factor, factor);
        }

        default void set(T x, T y) {
            setX(x);
            setY(y);
        }
        default void set(Vector<T> other) {
            setX(other.getX());
            setY(other.getY());
        }
    }
    interface Immutable<T extends Number> extends Vector<T> {

        @Override
        default void read(CompoundData data) {
            throw new UnsupportedOperationException("Immutable; cannot read " + data);
        }
    }

    class MutableInt implements Mutable<Integer>, Int {
        public int x;
        public int y;
        public MutableInt(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public MutableInt(CompoundData data) {
            read(data);
        }
        public MutableInt(Vector<?> other) {
            this.x = other.getX().intValue();
            this.y = other.getY().intValue();
        }

        @Override
        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putInt(X_KEY, x);
            data.putInt(Y_KEY, y);
            return data;
        }

        @Override
        public void read(CompoundData data) {
            x = data.getInt(X_KEY);
            y = data.getInt(Y_KEY);
        }

        @Override
        public Integer getX() {
            return x;
        }

        @Override
        public Integer getY() {
            return y;
        }

        @Override
        public void setX(Integer x) {
            this.x = x;
        }

        @Override
        public void setY(Integer y) {
            this.y = y;
        }

        @Override
        public MutableInt add(Integer x, Integer y) {
            this.x += x;
            this.y += y;
            return this;
        }

        @Override
        public MutableInt subtract(Integer x, Integer y) {
            this.x -= x;
            this.y -= y;
            return this;
        }

        @Override
        public MutableInt add(Vector<?> other) {
            return add(other.getX().intValue(), other.getY().intValue());
        }

        @Override
        public MutableInt subtract(Vector<?> other) {
            return subtract(other.getX().intValue(), other.getY().intValue());
        }

        @Override
        public Mutable<Integer> multiply(Vector<?> other) {
            return Mutable.super.multiply(other);
        }

        @Override
        public MutableInt multiply(double xFactor, double yFactor) {
            this.x *= xFactor;
            this.y *= yFactor;
            return this;
        }

        @Override
        public MutableInt scale(double factor) {
            return multiply(factor, factor);
        }

        @Override
        public ImmutableInt addedTo(Integer x, Integer y) {
            return new ImmutableInt(this.x + x, this.y + y);
        }

        @Override
        public ImmutableInt subtractedBy(Integer x, Integer y) {
            return new ImmutableInt(this.x - x, this.y - y);
        }

        @Override
        public ImmutableInt multipliedBy(double xFactor, double yFactor) {
            return new ImmutableInt((int) (x * xFactor), (int) (y * yFactor));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    class MutableDouble implements Mutable<java.lang.Double>, Double {
        public double x;
        public double y;
        public MutableDouble(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public MutableDouble(CompoundData data) {
            read(data);
        }
        public MutableDouble(Vector<?> other) {
            this.x = other.getX().doubleValue();
            this.y = other.getY().doubleValue();
        }

        @Override
        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putDouble(X_KEY, x);
            data.putDouble(Y_KEY, y);
            return data;
        }

        @Override
        public void read(CompoundData data) {
            x = data.getDouble(X_KEY);
            y = data.getDouble(Y_KEY);
        }

        @Override
        public java.lang.Double getX() {
            return x;
        }

        @Override
        public java.lang.Double getY() {
            return y;
        }

        @Override
        public void setX(java.lang.Double x) {
            this.x = x;
        }

        @Override
        public void setY(java.lang.Double y) {
            this.y = y;
        }

        @Override
        public MutableDouble add(java.lang.Double x, java.lang.Double y) {
            this.x += x;
            this.y += y;
            return this;
        }

        @Override
        public MutableDouble subtract(java.lang.Double x, java.lang.Double y) {
            this.x -= x;
            this.y -= y;
            return this;
        }

        @Override
        public MutableDouble add(Vector<?> other) {
            return add(other.getX().doubleValue(), other.getY().doubleValue());
        }

        @Override
        public MutableDouble subtract(Vector<?> other) {
            return subtract(other.getX().doubleValue(), other.getY().doubleValue());
        }

        @Override
        public MutableDouble multiply(double xFactor, double yFactor) {
            this.x *= xFactor;
            this.y *= yFactor;
            return this;
        }

        @Override
        public MutableDouble scale(double factor) {
            return multiply(factor, factor);
        }

        @Override
        public ImmutableDouble addedTo(java.lang.Double x, java.lang.Double y) {
            return new ImmutableDouble(this.x + x, this.y + y);
        }

        @Override
        public ImmutableDouble subtractedBy(java.lang.Double x, java.lang.Double y) {
            return new ImmutableDouble(this.x - x, this.y - y);
        }

        @Override
        public ImmutableDouble multipliedBy(double xFactor, double yFactor) {
            return new ImmutableDouble(x * xFactor, y * yFactor);
        }

        public MutableDouble normalize() {
            double length = length();
            x /= length;
            y /= length;
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    class ImmutableInt implements Immutable<Integer>, Int {
        public final int x;
        public final int y;
        public ImmutableInt(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public ImmutableInt(Vector<?> other) {
            this.x = other.getX().intValue();
            this.y = other.getY().intValue();
        }

        public ImmutableInt(CompoundData data) {
            x = data.getInt(X_KEY);
            y = data.getInt(Y_KEY);
        }

        @Override
        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putInt(X_KEY, x);
            data.putInt(Y_KEY, y);
            return data;
        }

        @Override
        public Integer getX() {
            return x;
        }

        @Override
        public Integer getY() {
            return y;
        }

        @Override
        public ImmutableInt addedTo(Integer x, Integer y) {
            return new ImmutableInt(this.x + x, this.y + y);
        }

        @Override
        public ImmutableInt subtractedBy(Integer x, Integer y) {
            return new ImmutableInt(this.x - x, this.y - y);
        }

        @Override
        public ImmutableInt multipliedBy(double xFactor, double yFactor) {
            return new ImmutableInt((int) (x * xFactor), (int) (y * yFactor));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    class ImmutableDouble implements Immutable<java.lang.Double>, Double {
        public final double x;
        public final double y;
        public ImmutableDouble(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public ImmutableDouble(Vector<?> other) {
            this.x = other.getX().doubleValue();
            this.y = other.getY().doubleValue();
        }

        public ImmutableDouble(CompoundData data) {
            x = data.getDouble(X_KEY);
            y = data.getDouble(Y_KEY);
        }

        @Override
        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putDouble(X_KEY, x);
            data.putDouble(Y_KEY, y);
            return data;
        }

        @Override
        public java.lang.Double getX() {
            return x;
        }

        @Override
        public java.lang.Double getY() {
            return y;
        }

        @Override
        public ImmutableDouble addedTo(java.lang.Double x, java.lang.Double y) {
            return new ImmutableDouble(this.x + x, this.y + y);
        }

        @Override
        public ImmutableDouble subtractedBy(java.lang.Double x, java.lang.Double y) {
            return new ImmutableDouble(this.x - x, this.y -  y);
        }

        @Override
        public ImmutableDouble multipliedBy(double xFactor, double yFactor) {
            return new ImmutableDouble(x * xFactor, y * yFactor);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
