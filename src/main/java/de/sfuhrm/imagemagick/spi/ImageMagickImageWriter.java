package de.sfuhrm.imagemagick.spi;

import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.IIOImage;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
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
        if (wand != null) {
            wand.close();
            wand = null;
        }
        try {
            this.wand = magick.new MagickWand();
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
        String formatMagickName = imageMagickImageWriterSpi.getMagickName();;

        try {
            DataBuffer dataBuffer = buffered.getData().getDataBuffer();
            if (dataBuffer instanceof DataBufferByte) {
                byte[] pixels = ((DataBufferByte) dataBuffer).getData();
                String mapName;
                int wordsInPixels;
                switch (buffered.getType()) {
                    case BufferedImage.TYPE_BYTE_GRAY:
                        mapName = "I";
                        wordsInPixels = 1;
                        break;
                    case BufferedImage.TYPE_3BYTE_BGR:
                        mapName = "BGR";
                        wordsInPixels = 3;
                        break;
                    case BufferedImage.TYPE_4BYTE_ABGR:
                        mapName = "ABGR";
                        wordsInPixels = 4;
                        break;
                    default:
                        throw new IOException("Could not detect type, BufferedImage.type==" + buffered.getType());
                };
                wand.newImage(buffered.getWidth(), buffered.getHeight());
                wand.importImagePixelsAsBytes(pixels, mapName, wordsInPixels, buffered.getWidth(), buffered.getHeight());
                wand.setImageFormat(formatMagickName);
                byte[] imageData = wand.getImageBlob();
                output.write(imageData);
            } else if (dataBuffer instanceof DataBufferUShort) {
                short[] pixels = ((DataBufferUShort) dataBuffer).getData();
                String mapName;
                int wordsInPixels;
                switch (buffered.getType()) {
                    case BufferedImage.TYPE_USHORT_GRAY:
                        mapName = "I";
                        wordsInPixels = 1;
                        break;
                    default:
                        switch (buffered.getData().getNumBands()) {
                            case 3:
                                mapName = "RGB";
                                wordsInPixels = 3;
                                break;
                            case 4:
                                mapName = "RGBA";
                                wordsInPixels = 4;
                                break;
                            default:
                                throw new IOException("Could not detect type, BufferedImage.type==" + buffered.getType());
                        }
                };
                wand.newImage(buffered.getWidth(), buffered.getHeight());
                wand.importImagePixelsAsShorts(pixels, mapName, wordsInPixels, buffered.getWidth(), buffered.getHeight());
                wand.setImageFormat(formatMagickName);
                byte[] imageData = wand.getImageBlob();
                output.write(imageData);
            } else {
                throw new IOException("Only 8 and 16 bit types are supported at the moment");
            }
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

    @Override
    public void dispose() {
        super.dispose();
        output = null;
        if (wand != null) {
            wand.close();
            wand = null;
        }
    }
}
