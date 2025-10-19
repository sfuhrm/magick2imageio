package de.sfuhrm.imagemagick.spi;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class TestImage {

    enum ImageReference {
        JPEG_HSV_8("JPEG_HSV_8.jpg", 3072, 4080),
        PNG_GRAY_8("PNG_GRAY_8.png", 256, 340),
        PNG_GRAY_16("PNG_GRAY_16.png", 256, 340),
        PNG_RGB_8("PNG_RGB_8.png", 256, 340),
        PNG_RGBA_8("PNG_RGBA_8.png", 256, 340),
        PNG_RGB_16("PNG_RGB_16.png", 256, 340),
        PNG_RGBA_16("PNG_RGBA_16.png", 256, 340);

        private String file;
        private int width;
        private int height;
        ImageReference(String file, int width, int height) {
            this.file = file;
            this.width = width;
            this.height = height;
        }

        String file() {
            return file;
        }

        int width() {
            return width;
        }

        int height() {
            return height;
        }
    }

    static byte[] readTestImage(ImageReference ref) throws IOException {
        try (InputStream is = NativeMagickTest.class.getResourceAsStream("/" + ref.file())) {
            return is.readAllBytes();
        }
    }

    static BufferedImage readWithJDK(ImageReference ref) throws IOException {
        byte[] data = readTestImage(ref);
        return readWithJDK(data, 0);
    }

    static BufferedImage readWithJDK(byte[] imageBlob, int imageIndex) throws IOException {
        InputStream stream = new ByteArrayInputStream(imageBlob);
        ImageInputStream iis = ImageIO.createImageInputStream(stream);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

        ImageReader selectedReader = null;
        while (readers.hasNext()) {
            ImageReader reader = readers.next();

            if (!(reader instanceof ImageMagickImageReader)) {
                selectedReader = reader;
                break;
            }
        }
        selectedReader.setInput(iis);
        BufferedImage result = selectedReader.read(imageIndex);
        iis.close();
        selectedReader.dispose();
        return result;
    }
}
