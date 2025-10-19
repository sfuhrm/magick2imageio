package de.sfuhrm.imagemagick.spi;

/** A consumer throwing an exception. */
interface ConsumerWithException<T> {
    void consume(T in) throws Throwable;
}
