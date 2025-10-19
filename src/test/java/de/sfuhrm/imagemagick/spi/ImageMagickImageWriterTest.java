package de.sfuhrm.imagemagick.spi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

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

    @Test
    public void setOutputWithGoodOutput() throws IOException {
        File tmpFile = File.createTempFile("img", "tmp");
        RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "rw");
        FileImageOutputStream imageOutputStream = new FileImageOutputStream(randomAccessFile);
        imageMagickImageWriter.setOutput(imageOutputStream);
        tmpFile.delete();
    }

    @Test
    public void writeWithRenderedImage() throws IOException {
        File tmpFile = File.createTempFile("img", "tmp");
        RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "rw");
        FileImageOutputStream imageOutputStream = new FileImageOutputStream(randomAccessFile);

        // setOutput
        imageMagickImageWriter.setOutput(imageOutputStream);
        TestImage.ImageReference imageReference = TestImage.ImageReference.JPEG_HSV_8;
        BufferedImage bufferedImage = TestImage.readWithJDK(imageReference);

        // write
        imageMagickImageWriter.write(bufferedImage);
        imageMagickImageWriter.dispose();

        // read back using JDK
        byte[] readback = Files.readAllBytes(tmpFile.toPath());
        BufferedImage readbackImage = TestImage.readWithJDK(readback, 0);

        // and compare
        assertEquals(bufferedImage.getWidth(), readbackImage.getWidth());
        assertEquals(bufferedImage.getHeight(), readbackImage.getHeight());

        tmpFile.delete();
    }

    private void writeWithRenderedImage(TestImage.ImageReference source) throws IOException {
        File tmpFile = File.createTempFile("img", "tmp");
        RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "rw");
        FileImageOutputStream imageOutputStream = new FileImageOutputStream(randomAccessFile);

        // setOutput
        imageMagickImageWriter.setOutput(imageOutputStream);
        BufferedImage expectedImage = TestImage.readWithJDK(source);

        // write
        imageMagickImageWriter.write(expectedImage);
        imageMagickImageWriter.dispose();

        // read back using ImageReader
        // NOTE: This will probably use the ImageMagickImageReader!
        BufferedImage actualImage = ImageIO.read(tmpFile);

        // and compare
        ImageAsserts.compareBufferedImages(expectedImage, actualImage, 1);

        tmpFile.delete();
    }

    /**
     * @see ImageMagickImageReader#read(int, ImageReadParam)
     */
    @ParameterizedTest(name = "{index} image {0}")
    @EnumSource(TestImage.ImageReference.class)
    public void writeWithRenderedImageMany( TestImage.ImageReference source) throws IOException {
        System.out.println("Image: " + source.file());
        writeWithRenderedImage(source);
    }
}
