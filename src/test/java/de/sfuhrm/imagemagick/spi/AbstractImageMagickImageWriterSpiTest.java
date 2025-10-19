package de.sfuhrm.imagemagick.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.ResourceBundle;

public class AbstractImageMagickImageWriterSpiTest {
    private AbstractImageMagickImageWriterSpi abstractImageMagickImageWriterSpi;

    @BeforeEach
    public void beforeEach() {
        ResourceBundle.clearCache();
        abstractImageMagickImageWriterSpi =
                new AbstractImageMagickImageWriterSpi("PNG", "png", "image/png");
    }

    @Test
    public void getDescription() {
        String msg = abstractImageMagickImageWriterSpi.getDescription(Locale.ENGLISH);
        assertNotNull(msg);
    }
}
