package de.sfuhrm.imagemagick.spi;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageReader;
import java.io.IOException;
import java.util.Locale;

/**
 * ImageMagickImageReaderSpi - Service Provider for ImageMagick-based ImageReader.
 *
 * Registers the ImageMagick-backed reader as an ImageIO plugin
 * for a wide range of formats supported by ImageMagick.
 */
public class ImageMagickImageReaderSpi extends ImageReaderSpi {


    private static final String[] NAMES = {
            "ImageMagick",
            "magick",
            "im",
            "convert"
    };

    private static final String READER_CLASS_NAME = "com.example.imagemagick.spi.ImageMagickImageReader";

    private static final String[] WRITER_SPI_NAMES = null; // no paired writer for now

    public ImageMagickImageReaderSpi() {
        super(
                SpiCommon.getProperties().getProperty(SpiCommon.NAME_PROPERTY, "unknown"),
                SpiCommon.getProperties().getProperty(SpiCommon.VERSION_PROPERTY, "unknown"),
                NAMES,
                SpiCommon.getSuffixes(),
                SpiCommon.getMimeTypes(),
                READER_CLASS_NAME,
                new Class[]{ImageInputStream.class},
                WRITER_SPI_NAMES,
                false,
                null, null, null, null,
                false,
                null, null, null, null);
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream stream)) {
            return false;
        }

        stream.mark();
        byte[] data = SpiCommon.readFully(stream);
        stream.reset();
        try {
            return NativeMagick.instance().canRead(data);
        } catch (MagickException e) {
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new ImageMagickImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        String message = "ImageMagick-based ImageWReader";
        return message;
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        super.onRegistration(registry, category);
    }
}
