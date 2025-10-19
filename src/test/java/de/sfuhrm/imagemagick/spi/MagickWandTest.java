package de.sfuhrm.imagemagick.spi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MagickWandTest {


    private NativeMagick.MagickWand wand;
    private byte[] imageBytes;
    private TestImage.ImageReference imageReference;

    @BeforeEach
    public void beforeEach() throws MagickException, IOException {
        NativeMagick nativeMagick = NativeMagick.instance();
        wand = nativeMagick.new MagickWand();
        imageReference = TestImage.ImageReference.JPEG_HSV_8;
        imageBytes = TestImage.readTestImage(imageReference);
    }

    @AfterEach
    public void afterEach() throws MagickException {
        if (wand != null) {
            wand.close();
        }
    }

    @Test
    public void readBlobWithJpeg() throws IOException, MagickException {
        wand.readBlob(imageBytes);
    }

    @Test
    public void getNumberImages() throws IOException, MagickException {
        long numImages = wand.getNumberImages();
        assertEquals(0, numImages);

        wand.readBlob(imageBytes);
        numImages = wand.getNumberImages();
        assertEquals(1, numImages);
    }

    @Test
    public void exportImagePixelsAsRGBBytes() throws IOException, MagickException {
        wand.readBlob(imageBytes);
        byte[] pixels = wand.exportImagePixelsAsRGBBytes();
        assertEquals(3 * imageReference.width() * imageReference.height(), pixels.length);
    }
}
