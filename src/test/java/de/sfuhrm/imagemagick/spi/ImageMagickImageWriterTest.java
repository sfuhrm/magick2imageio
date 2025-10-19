package de.sfuhrm.imagemagick.spi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageMagickImageWriterTest {

    private ImageMagickImageWriter imageMagickImageWriter;

    @BeforeEach
    public void beforeEach() {
        imageMagickImageWriter = new ImageMagickImageWriter(
                new AbstractImageMagickImageWriterSpi("PNG", "png", "image/png"));
    }

    @AfterEach
    public void afterEach() {
        imageMagickImageWriter.dispose();
    }

    @Test
    public void setOutputWithWrongOutput() {
        assertThrows(IllegalArgumentException.class,
                () -> imageMagickImageWriter.setOutput("foo"));
    }
}
