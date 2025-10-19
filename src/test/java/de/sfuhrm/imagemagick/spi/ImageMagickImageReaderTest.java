package de.sfuhrm.imagemagick.spi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class ImageMagickImageReaderTest {

    private ImageMagickImageReader imageMagickImageReader;

    @BeforeEach
    public void beforeEach() {
        imageMagickImageReader = new ImageMagickImageReader(new ImageMagickImageReaderSpi());
    }

    @AfterEach
    public void afterEach() {
        imageMagickImageReader.dispose();
    }

    @Test
    public void setInput() throws IOException {
        TestImage.ImageReference source = TestImage.ImageReference.JPEG_HSV_8;
        byte[] image = TestImage.readTestImage(source);

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));
        imageMagickImageReader.setInput(imageInputStream, false);

        int w = imageMagickImageReader.getWidth(0);
        int h = imageMagickImageReader.getHeight(0);

        assertEquals(source.width(), w);
        assertEquals(source.height(), h);
    }

    @Test
    public void getNumImages() throws IOException {
        byte[] image = TestImage.readTestImage(TestImage.ImageReference.JPEG_HSV_8);

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));
        imageMagickImageReader.setInput(imageInputStream, false);

        int num = imageMagickImageReader.getNumImages(true);
        assertEquals(1, num);
    }

    void readComparing(TestImage.ImageReference imageReference) throws IOException {
        byte[] image = TestImage.readTestImage(imageReference);

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));
        imageMagickImageReader.setInput(imageInputStream, false);

        BufferedImage actualImage = imageMagickImageReader.read(0);

        BufferedImage expectedImage = TestImage.readWithJDK(imageReference);
        assertEquals(imageReference.width(), actualImage.getWidth());
        assertEquals(imageReference.height(), actualImage.getHeight());

        // compare pixel by pixel
        ImageAsserts.compareBufferedImages(expectedImage, actualImage, 1);
    }

    @Test
    public void getImageTypes() throws IOException {
        // first image
        TestImage.ImageReference imageReference = TestImage.ImageReference.PNG_RGB_8;
        byte[] image = TestImage.readTestImage(imageReference);

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));
        imageMagickImageReader.setInput(imageInputStream, false);

        Iterator<ImageTypeSpecifier> iter = imageMagickImageReader.getImageTypes(0);

        assertTrue(iter.hasNext());
        ImageTypeSpecifier spec = iter.next();
        assertNotNull(spec);
        assertFalse(iter.hasNext());

        assertEquals(3, spec.getNumComponents());
        assertEquals(3, spec.getNumBands());
        assertEquals(8, spec.getBitsPerBand(0));
        assertEquals(8, spec.getBitsPerBand(1));
        assertEquals(8, spec.getBitsPerBand(2));
        assertNotNull(spec.getSampleModel());
        assertNotNull(spec.getColorModel());
    }

    @Test
    public void readWithMultiple() throws IOException {
        // first image
        TestImage.ImageReference imageReference = TestImage.ImageReference.PNG_RGB_8;
        byte[] image = TestImage.readTestImage(imageReference);

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));
        imageMagickImageReader.setInput(imageInputStream, false);

        BufferedImage actualImage = imageMagickImageReader.read(0);
        assertEquals(1, imageMagickImageReader.getNumImages(true));
        assertEquals(imageReference.width(), actualImage.getWidth());
        assertEquals(imageReference.height(), actualImage.getHeight());

        // second image
        imageReference = TestImage.ImageReference.JPEG_HSV_8;
        image = TestImage.readTestImage(imageReference);
        imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image));
        imageMagickImageReader.setInput(imageInputStream, false);

        actualImage = imageMagickImageReader.read(0);
        assertEquals(1, imageMagickImageReader.getNumImages(true));
        assertEquals(imageReference.width(), actualImage.getWidth());
        assertEquals(imageReference.height(), actualImage.getHeight());
    }

    /**
     * @see ImageMagickImageReader#read(int, ImageReadParam) 
     */
    @ParameterizedTest(name = "{index} image {0}")
    @EnumSource(TestImage.ImageReference.class)
    public void readWithMany( TestImage.ImageReference source) throws IOException {
        System.out.println("Image: " + source.file());
        readComparing(source);
    }
}
