package de.sfuhrm.imagemagick.spi;

interface SupplierWithException<T> {
    T get() throws Throwable;
}
