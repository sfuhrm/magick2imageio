package de.sfuhrm.imagemagick.spi;

import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.util.Locale;
import java.util.Objects;

/**
 * Service Provider for ImageMagick-based ImageWriter.
 *
 * Registers the ImageMagick-backed writer as an ImageIO plugin for
 * multiple output formats supported by ImageMagick. Each subclass
 * is responsible for one image format.
 */
public class AbstractImageMagickImageWriterSpi extends ImageWriterSpi {

    private final String magickName;
    private final String suffix;
    private final String mimeType;

    protected AbstractImageMagickImageWriterSpi(String magickName, String suffix, String mimeType) {
        super(
                SpiCommon.getProperties().getProperty(SpiCommon.NAME_PROPERTY, "unknown"),
                SpiCommon.getProperties().getProperty(SpiCommon.VERSION_PROPERTY, "unknown"),
                new String[] { magickName },
                new String[] { suffix },
                new String[] { mimeType },
                ImageMagickImageWriter.class.getName(),
                new Class[]{ImageOutputStream.class},
                new String[] { ImageMagickImageReaderSpi.class.getName() },
                false,
                null, null, null, null,
                false,
                null, null, null, null);
        this.magickName = Objects.requireNonNull(magickName);
        this.suffix = Objects.requireNonNull(suffix);
        this.mimeType = Objects.requireNonNull(mimeType);
    }

    protected String getMagickName() {
        return magickName;
    }

    protected String getSuffix() {
        return suffix;
    }

    protected String getMimeType() {
        return mimeType;
    }

    @Override
    public boolean canEncodeImage(javax.imageio.ImageTypeSpecifier type) {
        // Accept all image types (ImageMagick can convert nearly everything)
        return true;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) {
        return new ImageMagickImageWriter(this);
    }

    @Override
    public String getDescription(Locale locale) {
        String message = "ImageMagick-based ImageWriter for %s";
        return String.format(message, getMagickName());
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        super.onRegistration(registry, category);
        // Optional: adjust registration priority here
    }
}
