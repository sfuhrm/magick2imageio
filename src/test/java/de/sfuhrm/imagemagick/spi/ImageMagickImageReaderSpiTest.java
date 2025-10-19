package de.sfuhrm.imagemagick.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class ImageMagickImageReaderSpiTest {


    private ImageMagickImageReaderSpi imageMagickImageReaderSpi;

    @BeforeEach
    public void beforeEach() {
        imageMagickImageReaderSpi = new ImageMagickImageReaderSpi();
    }

    @Test
    public void newInstance() {
        new ImageMagickImageReaderSpi();
    }

    @Test
    public void canDecodeWithPNG() throws IOException {
        TestImage.ImageReference imageReference = TestImage.ImageReference.PNG_RGB_8;
        byte[] image = TestImage.readTestImage(imageReference);
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));

        boolean canDecode = imageMagickImageReaderSpi.canDecodeInput(imageInputStream);
        assertTrue(canDecode);
    }

    @Test
    public void canDecodeWithGarbage() throws IOException {
        byte[] image = new byte[1024];
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));
        boolean canDecode = imageMagickImageReaderSpi.canDecodeInput(imageInputStream);
        assertFalse(canDecode);
    }

    @Test
    public void createReaderInstance() {
        ImageReader reader = imageMagickImageReaderSpi.createReaderInstance(null);
        assertEquals(ImageMagickImageReader.class, reader.getClass());
    }

    @Test
    public void getDescription() {
        assertNotNull(imageMagickImageReaderSpi.getDescription(Locale.getDefault()));
    }
}
