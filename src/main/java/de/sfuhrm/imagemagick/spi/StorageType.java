package de.sfuhrm.imagemagick.spi;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.function.Function;

enum StorageType {
    UndefinedPixel(null, null, null),
    CharPixel(ValueLayout.JAVA_BYTE, n -> {
        var array = new byte[n];
        return new Pair(array, MemorySegment.ofArray(array));
    }, obj -> MemorySegment.ofArray((byte[]) obj)),
    DoublePixel(ValueLayout.JAVA_DOUBLE, n -> {
        var array = new double[n];
        return new Pair(array, MemorySegment.ofArray(array));
    }, obj -> MemorySegment.ofArray((double[]) obj)),
    FloatPixel(ValueLayout.JAVA_FLOAT, n -> {
        var array = new float[n];
        return new Pair(array, MemorySegment.ofArray(array));
    }, obj -> MemorySegment.ofArray((float[]) obj)),
    LongPixel(ValueLayout.JAVA_LONG, n -> {
        var array = new long[n];
        return new Pair(array, MemorySegment.ofArray(array));
    }, obj -> MemorySegment.ofArray((long[]) obj)),
    LongLongPixel(null, null, null),
    QuantumPixel(null, null, null),
    ShortPixel(ValueLayout.JAVA_SHORT, n -> {
        var array = new short[n];
        return new Pair(array, MemorySegment.ofArray(array));
    }, obj -> MemorySegment.ofArray((short[]) obj));

    /** The FFM API value layout for this storage type. */
    private final ValueLayout valueLayout;

    /** Function creating from an array size argument a pair of
     * Java array of the right type and
     * memory segment that corresponds to this Java array.
     * */
    private final Function<Integer, Pair<Object, MemorySegment>> newArrayFunction;

    /**
     * Function creating a MemorySegment out of the input Java array.
     */
    private final Function<Object, MemorySegment> fromArrayFunction;

    StorageType(
            ValueLayout valueLayout,
            Function<Integer, Pair<Object, MemorySegment>> newArrayFunction,
            Function<Object, MemorySegment> fromArrayFunction) {
        this.valueLayout = valueLayout;
        this.newArrayFunction = newArrayFunction;
        this.fromArrayFunction = fromArrayFunction;
    }

    ValueLayout getElementLayout() {
        return valueLayout;
    }

    Pair<Object, MemorySegment> newArray(int n) {
        return newArrayFunction.apply(n);
    }

    MemorySegment toMemorySegment(Object array) {
        return fromArrayFunction.apply(array);
    }
}
