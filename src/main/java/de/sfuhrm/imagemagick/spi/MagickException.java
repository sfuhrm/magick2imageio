package de.sfuhrm.imagemagick.spi;

import java.lang.foreign.MemorySegment;

class MagickException extends Exception {
    public MagickException(String message) {
        super(message);
    }

    public MagickException(Throwable e) {
        super(e);
    }

    public MagickException(String message, Throwable e) {
        super(message, e);
    }
}
