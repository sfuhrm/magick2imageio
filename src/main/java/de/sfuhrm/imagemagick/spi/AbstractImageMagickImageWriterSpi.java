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

    /** The name of the type in ImageMagick. */
    private final String magickName;

    /** The file name suffix for this type. */
    private final String suffix;

    /** The mime type of the type. */
    private final String mimeType;

    /**
     * Constructor for the ImageWriterSpi.
     * @param magickName name of the type in ImageMagick.
     * @param suffix file name suffix for this type.
     * @param mimeType mime type of the type.
     */
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

    /** Get the name of the type in ImageMagick. */
    protected String getMagickName() {
        return magickName;
    }

    /** Get the file name suffix for this type. */
    protected String getSuffix() {
        return suffix;
    }

    /** Get the mime type of the type. */
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
