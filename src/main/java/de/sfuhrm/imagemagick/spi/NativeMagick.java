package de.sfuhrm.imagemagick.spi;

import java.lang.foreign.Linker;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * FFM-based wrapper around key MagickWand functions using the
 * stable Java Foreign Function & Memory API (java.lang.foreign, JDK 22+).
 * @see <a href="https://imagemagick.org/api/magick-wand.php">MagickWand, C API: Wand Methods</a>
 * @see <a href="https://imagemagick.org/api/magick-image.php">MagickWand, C API: Image Methods</a>
 */
final class NativeMagick implements AutoCloseable {

    // Candidate library names (platform differences)
    private static final String[] LIB_NAMES = {
            "MagickWand",
            "MagickWand-7.Q16HDRI",
            "MagickWand-7",
            "libMagickWand-7.Q16.so.10",
            "libMagickWand-6.Q16.so.7"
    };

    private static final int MagickFalse = 0;
    private static final int MagickTrue = 1;

    private final Linker linker = Linker.nativeLinker();
    private final SymbolLookup lookup;

    // Function handles
    private final MethodHandle wandGenesis;
    private final MethodHandle wandTerminus;
    private final MethodHandle newWand;
    private final MethodHandle destroyWand;
    private final MethodHandle readImageBlob;
    private final MethodHandle setImageFormat;
    private final MethodHandle getImageBlob;
    private final MethodHandle relinquishMemory;
    private final MethodHandle getException;
    private final MethodHandle resetIterator;
    private final MethodHandle queryFormats;
    private final MethodHandle getNumberImages;
    private final MethodHandle getImageWidth;
    private final MethodHandle getImageHeight;
    private final MethodHandle getImageDepth;
    private final MethodHandle getImageColorspace;
    private final MethodHandle getImageAlphaChannel;
    private final MethodHandle setIteratorIndex;
    private final MethodHandle exportImagePixels;
    private final MethodHandle importImagePixels;
    private final MethodHandle newImage;
    private final MethodHandle newPixelWand;
    private final MethodHandle destroyPixelWand;

    public NativeMagick() {
        this.lookup = resolveLookup();

        wandGenesis = downcall("MagickWandGenesis", FunctionDescriptor.ofVoid());
        wandTerminus = downcall("MagickWandTerminus", FunctionDescriptor.ofVoid());
        newWand = downcall("NewMagickWand", FunctionDescriptor.of(ValueLayout.ADDRESS));
        destroyWand = downcall("DestroyMagickWand",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        readImageBlob = downcall("MagickReadImageBlob",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        setImageFormat = downcall("MagickSetImageFormat",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        getImageBlob = downcall("MagickGetImageBlob",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        relinquishMemory = downcall("MagickRelinquishMemory",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        getException = downcall("MagickGetException",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        resetIterator = downcall("MagickResetIterator",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        queryFormats = downcall("MagickQueryFormats",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        getNumberImages = downcall("MagickGetNumberImages",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        getImageWidth = downcall("MagickGetImageWidth",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        getImageHeight = downcall("MagickGetImageHeight",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        getImageDepth = downcall("MagickGetImageDepth",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        getImageColorspace = downcall("MagickGetImageColorspace",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        getImageAlphaChannel = downcall("MagickGetImageAlphaChannel",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        setIteratorIndex = downcall("MagickSetIteratorIndex",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        exportImagePixels = downcall("MagickExportImagePixels",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS));
        importImagePixels = downcall("MagickImportImagePixels",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS));
        newImage = downcall("MagickNewImage",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS));
        newPixelWand = downcall("NewPixelWand",
                FunctionDescriptor.of(ValueLayout.ADDRESS));
        destroyPixelWand = downcall("DestroyPixelWand",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        // Initialize the MagickWand environment if available
        try {
            wandGenesis.invoke();
        } catch (Throwable ignored) {
        }
    }

    static NativeMagick INSTANCE;
    synchronized static NativeMagick instance() {
        if (INSTANCE == null) {
            INSTANCE = new NativeMagick();
        }
        return INSTANCE;
    }

    private SymbolLookup resolveLookup() {
        // Try library lookups by name; fallback to default process symbols
        for (String lib : LIB_NAMES) {
            try {
                return SymbolLookup.libraryLookup(lib, Arena.global());
            } catch (Throwable ignored) {
            }
        }
        return linker.defaultLookup();
    }

    private MethodHandle downcall(String symbol, FunctionDescriptor fd) {
        return lookup.find(symbol)
                .map(addr -> linker.downcallHandle(addr, fd))
                .orElseThrow(() -> new UnsatisfiedLinkError("Missing symbol: " + symbol));
    }

    private Optional<MethodHandle> findOptional(String symbol, FunctionDescriptor fd) {
        return lookup.find(symbol).map(addr -> linker.downcallHandle(addr, fd));
    }

    @Override
    public void close() {
        try {
            wandTerminus.invoke();
        } catch (Throwable t) {
            // ignore
        }
    }

    /** ImageMagick wand or image context. */
    class MagickWand implements AutoCloseable {

        /** A wand is an image context. */
        private final MemorySegment wand;

        /** Creates a new empty image context. */
        MagickWand() throws MagickException {
            try {
                this.wand = (MemorySegment) newWand.invoke();
                if (wand == null || wand.equals(MemorySegment.NULL))
                    throw new MagickException("NewMagickWand failed");
            } catch (Throwable e) {
                throw new MagickException(e);
            }
        }

        /** Calls a function with a FFM memory arena. */
        private static <O> O callWithArena(FunctionWithException<Arena, O> func) throws MagickException {
            try (Arena arena = Arena.ofConfined()) {
                return func.apply(arena);
            } catch (Throwable t) {
                if (t instanceof MagickException) {
                    throw (MagickException) t;
                } else {
                    throw new MagickException(t);
                }
            }
        }

        /** Consumes a  FFM memory arena. */
        private static void withArena(ConsumerWithException<Arena> consumer) throws MagickException {
            callWithArena(arena -> { consumer.consume(arena); return null; });
        }

        /** Disposes the wand. */
        private void destroy(MemorySegment wand) {
            try {
                destroyWand.invoke(wand);
            } catch (Throwable ignored) {
            }
        }

        /** Reads the blob into the wand. */
        void readBlob(byte[] inputBlob) throws MagickException {
            Objects.requireNonNull(inputBlob, "inputBlob needs to be non-null");
            withArena(arena -> {
                MemorySegment inBuf = arena.allocateFrom(ValueLayout.JAVA_BYTE, inputBlob);
                int ok = invokeWithExceptionHandling(Integer.class,
                        () -> readImageBlob.invoke(wand, inBuf, (long) inputBlob.length));
                if (ok == MagickFalse) {
                    throw new MagickException("MagickReadImageBlob failed");
                }
            });
        }

        /** Gets the current image file in the specified format.
         * @see #setImageFormat(String)
         * */
        byte[] getImageBlob() throws MagickException {
            return callWithArena(arena -> {
                MemorySegment sizePtr = arena.allocate(ValueLayout.JAVA_LONG);
                MemorySegment blobPtr = (MemorySegment) getImageBlob.invoke(wand, sizePtr);
                long len = sizePtr.get(ValueLayout.JAVA_LONG, 0);
                if (blobPtr == null || blobPtr.equals(MemorySegment.NULL) || len <= 0) {
                    throw new MagickException("MagickGetImageBlob returned null/empty");
                }

                if (len > Integer.MAX_VALUE)
                    throw new MagickException("Image too large");

                byte[] out = new byte[(int) len];
                MemorySegment.ofArray(out).copyFrom(blobPtr.reinterpret(len).asSlice(0, len));

                relinquishMemory.invoke(blobPtr);

                return out;
            });
        }

        /** Resets the iterator. */
        void resetIterator() throws MagickException {
            invokeWithExceptionHandling(Void.class, () -> resetIterator.invoke(wand));
        }

        /** Sets the target image format for conversion. */
        void setImageFormat(String outFormat) throws MagickException {
            Objects.requireNonNull(outFormat, "outFormat name must be non-null");
            withArena(arena -> {
                MemorySegment fmt = arena.allocateFrom(outFormat);
                invokeWithMagickBool(
                    () -> setImageFormat.invoke(wand, fmt));
            });
        }

        /** Gets the number of images. */
        long getNumberImages() throws MagickException {
            return invokeWithExceptionHandling(Long.class,
                    () -> getNumberImages.invoke(wand));
        }

        /** Gets the image width. */
        long getImageWidth() throws MagickException {
            return invokeWithExceptionHandling(Long.class,
                    () -> getImageWidth.invoke(wand));
        }

        /** Gets the image height. */
        long getImageHeight() throws MagickException {
            return invokeWithExceptionHandling(Long.class,
                    () -> getImageHeight.invoke(wand));
        }

        /** Gets the image depth in bits per pixel. */
        int getImageDepth() throws MagickException {
            return invokeWithExceptionHandling(Long.class,
                    () -> getImageDepth.invoke(wand)).intValue();
        }

        ColorspaceType getImageColorspace() throws MagickException {
            return invokeWithExceptionHandling(ColorspaceType.class,
                    () -> {
                        long colorSpaceValue = (long) getImageColorspace.invoke(wand);
                        ColorspaceType[] values = ColorspaceType.values();
                        if (colorSpaceValue >= 0 && colorSpaceValue < values.length) {
                            return values[(int) colorSpaceValue];
                        } else {
                            throw new MagickException("Could not map color space value " + colorSpaceValue);
                        }
            });
        }

        boolean getImageAlphaChannel() throws MagickException {
            return invokeWithExceptionHandling(Boolean.class,
                    () -> {
                        long alphaChannel = (long) getImageAlphaChannel.invoke(wand);
                        return MagickTrue == alphaChannel;
                    });
        }

        /** Sets the image iterator index. */
        void setIteratorIndex(long index) throws MagickException {
            invokeWithMagickBool(
                    () -> setIteratorIndex.invoke(wand, index));
        }

        byte[] exportImagePixelsAsGrayBytes() throws MagickException {
            return (byte[]) exportImagePixels("I", 1, StorageType.CharPixel);
        }

        short[] exportImagePixelsAsGrayShorts() throws MagickException {
            return (short[]) exportImagePixels("I", 1, StorageType.ShortPixel);
        }

        byte[] exportImagePixelsAsRGBBytes() throws MagickException {
            return (byte[]) exportImagePixels("RGB", 3, StorageType.CharPixel);
        }

        byte[] exportImagePixelsAsARGBBytes() throws MagickException {
            return (byte[]) exportImagePixels("ARGB", 4, StorageType.CharPixel);
        }

        short[] exportImagePixelsAsRGBShorts() throws MagickException {
            return (short[]) exportImagePixels("RGB", 3, StorageType.ShortPixel);
        }

        short[] exportImagePixelsAsARGBShorts() throws MagickException {
            return (short[]) exportImagePixels("ARGB", 4, StorageType.ShortPixel);
        }

        private Object exportImagePixels(String mapName,
                                         int wordsPerPixel,
                                         StorageType wordType) throws MagickException {
            long width = getImageWidth();
            long height = getImageHeight();
            return callWithArena(arena -> {
                // This string reflects the expected ordering of the pixel array. It can be any combination or order of R = red, G = green, B = blue, A = alpha (0 is transparent), O = alpha (0 is opaque), C = cyan, Y = yellow, M = magenta, K = black, I = intensity (for grayscale), P = pad.
                MemorySegment map = arena.allocateFrom(mapName);
                long wordCount =
                        wordsPerPixel *
                                width * height;
                long byteCount = wordType.getElementLayout().byteSize() *
                        wordCount;
                MemorySegment pixels = arena.allocate(byteCount);
                invokeWithMagickBool(
                        () -> exportImagePixels.invoke(wand,
                                0, 0,
                                width, height,
                                map,
                                wordType.ordinal(),
                                pixels
                        ));

                Pair<Object, MemorySegment> pair = wordType.newArray((int) wordCount);
                pair.right().copyFrom(pixels.reinterpret(byteCount).asSlice(0, byteCount));
                return pair.left();
            });
        }

        void newImage(int width, int height) throws MagickException {
            callWithArena(arena -> {
                MemorySegment pixelWand = (MemorySegment) newPixelWand.invoke();
                invokeWithMagickBool(
                        () -> newImage.invoke(wand,
                                width, height,
                                pixelWand
                        ));
                destroyPixelWand.invoke(pixelWand);
                return 0;
            });
        }

        void importImagePixelsAsBytes(byte[] pixels, String mapName, int wordsPerPixel, int width, int height) throws MagickException {
            importImagePixels(pixels, mapName, wordsPerPixel, width, height, StorageType.CharPixel);
        }

        void importImagePixelsAsShorts(short[] pixels, String mapName, int wordsPerPixel, int width, int height) throws MagickException {
            importImagePixels(pixels, mapName, wordsPerPixel, width, height, StorageType.ShortPixel);
        }

        private void importImagePixels(
                Object pixelsArray,
                String mapName,
                int wordsPerPixel,
                int width,
                int height,
                StorageType wordType) throws MagickException {
            callWithArena(arena -> {
                // This string reflects the expected ordering of the pixel array. It can be any combination or order of R = red, G = green, B = blue, A = alpha (0 is transparent), O = alpha (0 is opaque), C = cyan, Y = yellow, M = magenta, K = black, I = intensity (for grayscale), P = pad.
                MemorySegment map = arena.allocateFrom(mapName);
                long wordCount =
                        wordsPerPixel *
                        width * height;
                long byteCount = wordType.getElementLayout().byteSize() *
                        wordCount;
                MemorySegment pixelsInHeap = wordType.toMemorySegment(pixelsArray);
                MemorySegment pixelsOffHeap = arena.allocate(byteCount);
                MemorySegment.copy(pixelsInHeap, 0, pixelsOffHeap, 0, byteCount);

                invokeWithMagickBool(
                        () -> importImagePixels.invoke(wand,
                                0, 0,
                                width, height,
                                map,
                                wordType.ordinal(),
                                pixelsOffHeap
                        ));
                return 0;
            });
        }

        public void close() {
            if (wand != null) {
                destroy(wand);
            }
        }

        private void invokeWithMagickBool(SupplierWithException<Object> invocation) throws MagickException {
            try {
                int ok = (int) invocation.get();
                if (ok == MagickFalse) {
                    checkMagickException();
                    throw new MagickException("Operation failed");
                }
            } catch (Throwable e) {
                throw new MagickException(e);
            }
        }

        private <R> R invokeWithExceptionHandling(Class<R> resultClass, SupplierWithException<Object> invocation) throws MagickException {
            try {
                return resultClass.cast(invocation.get());
            } catch (Throwable e) {
                throw new MagickException(e);
            }
        }

        private void checkMagickException() throws MagickException {
            try {
                withArena(arena -> {
                    MemorySegment severityPtr = arena.allocate(ValueLayout.JAVA_LONG);
                    MemorySegment exceptionMessage = (MemorySegment) getException.invoke(wand, severityPtr);
                    String exceptString = exceptionMessage.reinterpret(Long.MAX_VALUE).getString(0, StandardCharsets.US_ASCII);
                    relinquishMemory.invoke(exceptionMessage);
                    if (exceptString != null && !exceptString.isEmpty()) {
                        throw new MagickException(exceptString);
                    }
                });
            } catch (MagickException e) {
                throw e;
            }
            catch (Throwable e) {
                // ignore
            }
        }

        private MagickException newMagickException(MagickException e)  {
            try {
                return callWithArena(arena -> {
                    MemorySegment severityPtr = arena.allocate(ValueLayout.JAVA_LONG);
                    try {
                        MemorySegment exceptionMessage = (MemorySegment) getException.invoke(wand, severityPtr);
                        String exceptString = exceptionMessage.reinterpret(Long.MAX_VALUE).getString(0, StandardCharsets.US_ASCII);
                        relinquishMemory.invoke(exceptionMessage);
                        if (exceptString != null && !exceptString.isEmpty()) {
                            return new MagickException(exceptString, e);
                        } else {
                            return e;
                        }
                    }
                    catch (Throwable t) {
                        return new MagickException(e.getMessage());
                    }
                });
            } catch (MagickException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Convert an image blob using ImageMagick.
     *
     * @param inputBlob input image bytes (any format Magick supports)
     * @param outFormat output format (e.g. \"PNG\", \"JPEG\", \"WEBP\")
     */
    byte[] convertBlob(byte[] inputBlob, String outFormat) throws MagickException {
        Objects.requireNonNull(inputBlob, "input blob must be non-null");
        Objects.requireNonNull(outFormat, "outFormat name must be non-null");
        try (MagickWand wand = new MagickWand()) {
            try {
                wand.readBlob(inputBlob);
                wand.setImageFormat(outFormat);
                wand.resetIterator();

                return wand.callWithArena(arena -> {
                    MemorySegment sizePtr = arena.allocate(ValueLayout.JAVA_LONG);
                    MemorySegment blobPtr = (MemorySegment) getImageBlob.invoke(wand.wand, sizePtr);
                    long len = sizePtr.get(ValueLayout.JAVA_LONG, 0);
                    if (blobPtr == null || blobPtr.equals(MemorySegment.NULL) || len <= 0) {
                        throw new MagickException("MagickGetImageBlob returned null/empty");
                    }

                    if (len > Integer.MAX_VALUE)
                        throw new MagickException("Image too large");

                    byte[] out = new byte[(int) len];
                    MemorySegment.ofArray(out).copyFrom(blobPtr.reinterpret(len).asSlice(0, len));

                    relinquishMemory.invoke(blobPtr);

                    return out;
                });
            }
            catch (MagickException e) {
                throw wand.newMagickException(e);
            }
        } catch (Throwable e) {
            throw new MagickException(e);
        }
    }

    /**
     * Test whether the image can be read.
     *
     * @param inputBlob input image bytes (any format Magick supports)
     */
    boolean canRead(byte[] inputBlob) throws MagickException {
        Objects.requireNonNull(inputBlob, "input blob must be non-null");
        try (MagickWand wand = new MagickWand()) {
            try {
                wand.readBlob(inputBlob);
                return true;
            }
            catch (MagickException e) {
                return false;
            }
        }
    }

    /**
     * List available formats.
     */
    Set<String> queryFormats() throws MagickException {
        try (MagickWand wand = new MagickWand()) {
            return wand.callWithArena(arena -> {
                MemorySegment patternBuf = arena.allocateFrom("*");
                MemorySegment sizePtr = arena.allocate(ValueLayout.JAVA_LONG);
                try {
                    MemorySegment formats = (MemorySegment) queryFormats.invoke(patternBuf, sizePtr);
                    long numberOfFormats = sizePtr.get(ValueLayout.JAVA_LONG, 0);
                    formats = formats.reinterpret(ValueLayout.ADDRESS.byteSize() * numberOfFormats);
                    TreeSet<String> result = new TreeSet<>();
                    for (int i = 0; i < numberOfFormats; i++) {
                        String formatName = formats.reinterpret(Long.MAX_VALUE).get(ValueLayout.ADDRESS, i * ValueLayout.ADDRESS.byteSize()).reinterpret(Long.MAX_VALUE).getString(0);
                        result.add(formatName);
                    }

                    relinquishMemory.invoke(formats);
                    return result;
                } catch (Throwable e) {
                    throw new MagickException(e);
                }
            });
        }
    }
}
