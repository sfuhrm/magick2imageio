package de.sfuhrm.imagemagick.spi;

/** A function throwing an exception. */
interface FunctionWithException<I, O> {
    O apply(I in) throws Throwable;
}
