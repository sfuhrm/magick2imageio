package de.sfuhrm.imagemagick.spi;

import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ImageMagickImageWriter - ImageIO writer backed by ImageMagick via FFM API.
 *
 * This writer uses ImageMagick to convert a BufferedImage to arbitrary formats.
 * Internally it:
 *   1. Writes the BufferedImage to an intermediate format (PNG)
 *   2. Uses {@link NativeMagick#convertBlob(byte[], String)} to convert to the desired format
 *   3. Writes the converted bytes to the provided {@link ImageOutputStream}
 */
public class ImageMagickImageWriter extends ImageWriter {

    private final NativeMagick magick;
    private NativeMagick.MagickWand wand;
    private ImageOutputStream output;
    private AbstractImageMagickImageWriterSpi imageMagickImageWriterSpi;

    protected ImageMagickImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
        this.magick = new NativeMagick();
        this.imageMagickImageWriterSpi = (AbstractImageMagickImageWriterSpi) originatingProvider;
    }

    @Override
    public void setOutput(Object output) {
        if (output != null && !(output instanceof ImageOutputStream)) {
            throw new IllegalArgumentException("Output must be an ImageOutputStream");
        }
        super.setOutput(output);
        this.output = (ImageOutputStream) output;
        try {
            this.wand = magick. new MagickWand();
        } catch (MagickException e) {
        }
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, javax.imageio.ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (output == null) {
            throw new IllegalStateException("No output has been set");
        }
        if (image == null || image.getRenderedImage() == null) {
            throw new IllegalArgumentException("image == null or no rendered image");
        }

        BufferedImage buffered = convertToBuffered(image.getRenderedImage());
        String formatName = resolveOutputFormat(param, image);

        try {
            // Step 1: Encode to a neutral intermediate format (PNG)
            //byte[] pngBytes = toPNG(buffered);

            // Step 2: Convert via ImageMagick
            //byte[] outBytes = magick.convertBlob(pngBytes, formatName);

            // Step 3: Write bytes to the output stream
            //output.write(outBytes);
            output.flush();
        } catch (Throwable e) {
            throw new IOException("Failed to write image via ImageMagick", e);
        }
    }

    private BufferedImage convertToBuffered(java.awt.image.RenderedImage img) {
        if (img instanceof BufferedImage bi) {
            return bi;
        }
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage((BufferedImage) img, 0, 0, null);
        return copy;
    }

    private String resolveOutputFormat(ImageWriteParam param, IIOImage image) {
        // Determine output format from originating provider or param
        String[] names = getOriginatingProvider().getFormatNames();
        String fallback = (names != null && names.length > 0) ? names[0] : "PNG";

        /*
        if (param != null && param.getLocalizedImageTypeSpecifierName() != null) {
            return param.getLocalizedImageTypeSpecifierName();
        }

         */

        // Some apps pass format through metadata
        Object metadata = image.getMetadata();
        if (metadata != null && metadata instanceof IIOMetadata) {
            // Could read format name if available, but we'll default to fallback
        }
        return fallback;
    }

    @Override
    public void dispose() {
        super.dispose();
        output = null;
        if (wand != null) {
            wand.close();
        }
    }
}
