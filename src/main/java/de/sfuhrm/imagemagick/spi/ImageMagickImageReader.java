package de.sfuhrm.imagemagick.spi;

import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * ImageMagickImageReader - bridges ImageIO to ImageMagick (via Foreign Function API).
 *
 * This reader delegates the actual decoding of image bytes to {@link NativeMagick}
 * and then uses ImageIO to re-parse the converted bytes (e.g. as PNG or BMP)
 * into a standard {@link BufferedImage}.
 */
public class ImageMagickImageReader extends ImageReader {

    private final NativeMagick magick;
    private NativeMagick.MagickWand wand;
    private ImageInputStream stream;
    private byte[] inputData;
    private boolean hasData;

    protected ImageMagickImageReader(ImageReaderSpi originatingProvider) {
        this(originatingProvider, NativeMagick.instance());
    }

    protected ImageMagickImageReader(ImageReaderSpi originatingProvider, NativeMagick nativeMagick) {
        super(originatingProvider);
        this.magick = nativeMagick;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        try {
            ensureLoaded();
            return (int) wand.getNumberImages();
        } catch (MagickException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        if (imageIndex < 0) throw new IndexOutOfBoundsException();
        ensureLoaded();
        try {
            wand.setIteratorIndex(imageIndex);
            return (int) wand.getImageWidth();
        } catch (MagickException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        if (imageIndex < 0) throw new IndexOutOfBoundsException();
        ensureLoaded();
        try {
            wand.setIteratorIndex(imageIndex);
            return (int) wand.getImageHeight();
        } catch (MagickException e) {
            throw new IOException(e);
        }
    }


    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);

        hasData = false;
        if (wand != null) {
            wand.close();
            wand = null;
        }

        try {
            this.wand = magick.new MagickWand();
        } catch (MagickException e) {
        }

        this.stream = (ImageInputStream) input;
        this.inputData = null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        ensureLoaded();
        try {
            wand.setIteratorIndex(imageIndex);
            int width = (int) wand.getImageWidth();
            int height = (int) wand.getImageHeight();
            int imageDepth = wand.getImageDepth();
            ColorspaceType colorspaceType = wand.getImageColorspace();
            boolean hasAlpha = wand.getImageAlphaChannel();

            DataBuffer dataBuffer;
            switch (imageDepth) {
                case 8:
                    switch (colorspaceType.getChannelCount()) {
                        case 1: // Gray
                            dataBuffer = new DataBufferByte(wand.exportImagePixelsAsGrayBytes(), width * height, 0);
                            //return toBufferedImageGray(dataBuffer, BufferedImage.TYPE_BYTE_GRAY, width, height);
                            return toBufferedImage(dataBuffer, BufferedImage.TYPE_BYTE_GRAY, width, height, 1, new int[] {0});
                        case 3: // RGB
                        case 4: // ARGB
                            if (hasAlpha) {
                                dataBuffer = new DataBufferByte(wand.exportImagePixelsAsARGBBytes(), 4 * width * height, 0);
                                // TODO 1 2 3 1 doesnt make sense, but the test is green?
                                return toBufferedImage(dataBuffer, BufferedImage.TYPE_4BYTE_ABGR, width, height, 4, new int[] {1, 2, 3, 1});

                            } else {
                                dataBuffer = new DataBufferByte(wand.exportImagePixelsAsRGBBytes(), 3 * width * height, 0);
                                return toBufferedImage(dataBuffer, BufferedImage.TYPE_3BYTE_BGR, width, height, 3, new int[] {0, 1, 2});
                            }
                        default:
                            throw new IOException(
                                    "Handling imageDepth "
                                            + imageDepth
                                            + " and channel count "
                                            + colorspaceType.getChannelCount()
                                            + " not implemented");
                    }
                case 16:
                    switch (colorspaceType.getChannelCount()) {
                        case 1: // Gray
                            dataBuffer = new DataBufferUShort(wand.exportImagePixelsAsGrayShorts(), width * height, 0);
                            return toBufferedImage(dataBuffer, BufferedImage.TYPE_USHORT_GRAY, width, height, 1, new int[] {0});
                        case 3: // RGB
                        case 4: // ARGB
                            if (hasAlpha) {
                                dataBuffer = new DataBufferUShort(wand.exportImagePixelsAsARGBShorts(), 4 * width * height, 0);
                                return createRGBImageFromShorts((DataBufferUShort)  dataBuffer, width, height, new int[] {1, 2, 3, 1}, new int[] {16, 16, 16, 16}, 4);
                            } else {
                                dataBuffer = new DataBufferUShort(wand.exportImagePixelsAsRGBShorts(), 3 * width * height, 0);
                                return createRGBImageFromShorts((DataBufferUShort)  dataBuffer, width, height, new int[] {0, 1, 2}, new int[] {16, 16, 16}, 3);
                            }
                        default:
                            throw new IOException(
                                    "Handling imageDepth "
                                            + imageDepth
                                            + " and channel count "
                                            + colorspaceType.getChannelCount()
                                            + " not implemented");
                    }
                default:
                throw new IOException("Imagedepth " + imageDepth + " unsupported");
            }
        } catch (MagickException e) {
            throw new IOException(e);
        }
    }

    private static BufferedImage createRGBImageFromShorts(
            DataBufferUShort dataBuffer,
            int width,
            int height,
            int[] bandOffsets,
            int[] bits,
            int numBands) {

        int pixelStride = numBands;
        int scanlineStride = width * pixelStride;

        SampleModel sampleModel = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_USHORT,
                width,
                height,
                pixelStride,
                scanlineStride,
                bandOffsets
        );

        int colorSpaceType;
        int transparencyType;
        boolean hasAlpha;
        switch (numBands) {
            case 3:
                colorSpaceType = ColorSpace.CS_sRGB;
                transparencyType = Transparency.OPAQUE;
                hasAlpha = false;
                break;
            case 4:
                colorSpaceType = ColorSpace.CS_sRGB;
                transparencyType = Transparency.TRANSLUCENT;
                hasAlpha = true;
                break;
            default:
                throw new IllegalStateException();
        }

        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        ColorSpace colorSpace = ColorSpace.getInstance(colorSpaceType);

        ColorModel colorModel = new ComponentColorModel(
                colorSpace,
                bits,
                hasAlpha,
                false,
                transparencyType,
                DataBuffer.TYPE_USHORT
        );

        return new BufferedImage(colorModel, raster, false, null);
    }

    /**
     * Convert the data buffer to a BufferedImage.
     * @param dataBuffer data buffer to use.
     * @param bufferedImageType the type of the BufferedImage to create.
     * @param width width of image in pixels.
     * @param height height of image in pixels
     * @param numBands number of bands per pixel.
     * @param bandOffsets the offsets of each gun in the array.
     * @return the created buffered image.
     */
    private static BufferedImage toBufferedImage(DataBuffer dataBuffer, int bufferedImageType, int width, int height, int numBands, int[] bandOffsets) {
        int pixelStride = numBands;
        int scanlineStride = width * pixelStride;

        WritableRaster raster = Raster.createInterleavedRaster(
                dataBuffer,
                width,
                height,
                scanlineStride,
                pixelStride,
                bandOffsets,
                null
        );

        BufferedImage image = new BufferedImage(width, height, bufferedImageType);
        image.setData(raster);

        return image;
    }

    private void ensureLoaded() throws IOException {
        if (hasData) return;
        if (stream == null) throw new IllegalStateException("No input set");

        inputData = SpiCommon.readFully(stream);
        try {
            wand.readBlob(inputData);
            hasData = true;
        } catch (MagickException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (wand != null) {
            wand.close();
            wand = null;
        }
        inputData = null;
        hasData = false;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        ensureLoaded();
        BufferedImage image = read(imageIndex);
        ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(image);
        return Arrays.asList(imageTypeSpecifier).iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        // This reader does not provide stream-level metadata
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        // This reader does not provide image-level metadata
        return null;
    }
}
