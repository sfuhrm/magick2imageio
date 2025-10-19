package de.sfuhrm.imagemagick.spi;

interface FunctionWithException<I, O> {
    O apply(I in) throws Throwable;
}
