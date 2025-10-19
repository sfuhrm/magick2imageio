package de.sfuhrm.imagemagick.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class NativeMagickTest {

    private TestImage.ImageReference imageReference;
    private byte[] imageBytes;

    @BeforeEach
    public void beforeEach() throws MagickException, IOException {
        imageReference = TestImage.ImageReference.JPEG_HSV_8;
        imageBytes = TestImage.readTestImage(imageReference);
    }


    @Test
    public void newInstance() {
        NativeMagick instance = new NativeMagick();
        assertNotNull(instance);
    }

    @Test
    public void close() {
        NativeMagick instance = new NativeMagick();
        assertNotNull(instance);
        instance.close();
    }

    @Test
    public void canReadWithJpeg() throws Throwable {
        NativeMagick instance = new NativeMagick();
        assertTrue(instance.canRead(imageBytes));
    }

    @Test
    public void canReadWithGarbage() throws Throwable {
        NativeMagick instance = new NativeMagick();
        assertFalse(instance.canRead("you can not read garbage".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void convertBlobWithNullInputBlob() throws Throwable {
        NativeMagick instance = new NativeMagick();
        assertThrows(NullPointerException.class, () ->
            instance.convertBlob(null, "avif"));
    }

    @Test
    public void convertBlobWithNullFormatBlob() throws Throwable {
        NativeMagick instance = new NativeMagick();
        assertThrows(NullPointerException.class, () ->
                instance.convertBlob(new byte[0], null));
    }

    @Test
    public void convertBlobWithSampleJpegToJpeg() throws Throwable {
        NativeMagick instance = new NativeMagick();
        byte[] jpegBlob = imageBytes;
        byte[] output = instance.convertBlob(jpegBlob, "JPG");
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(output));
        assertEquals(imageReference.width(), image.getWidth());
        assertEquals(imageReference.height(), image.getHeight());
    }

    @Test
    public void convertBlobWithSampleJpegToUnknownFormat() throws Throwable {
        NativeMagick instance = new NativeMagick();
        byte[] jpegBlob = imageBytes;
        assertThrows(MagickException.class, () -> instance.convertBlob(jpegBlob, "12312312312313"));
    }

    @Test
    public void convertBlobWithSampleJpegToPNG() throws Throwable {
        NativeMagick instance = new NativeMagick();
        byte[] jpegBlob = imageBytes;
        byte[] output = instance.convertBlob(jpegBlob, "PNG");
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(output));
        assertEquals(imageReference.width(), image.getWidth());
        assertEquals(imageReference.height(), image.getHeight());
    }

    @Test
    public void convertBlobWithSampleJpegToAVIF() throws Throwable {
        NativeMagick instance = new NativeMagick();
        byte[] jpegBlob = imageBytes;
        byte[] output = instance.convertBlob(jpegBlob, "AVIF");
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(output));
        ImageIO.write(image, "AVIF", new File("/tmp/foo.avif"));
    }

    @Test
    public void queryFormats() throws Throwable {
        NativeMagick instance = new NativeMagick();
        Set<String> formats = instance.queryFormats();
        assertNotNull(formats);
        assertFalse(formats.isEmpty());
    }
}
