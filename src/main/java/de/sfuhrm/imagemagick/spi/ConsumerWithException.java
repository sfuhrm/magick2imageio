package de.sfuhrm.imagemagick.spi;

interface ConsumerWithException<T> {
    void consume(T in) throws Throwable;
}
