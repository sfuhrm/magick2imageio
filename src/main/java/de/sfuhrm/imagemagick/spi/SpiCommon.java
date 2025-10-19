package de.sfuhrm.imagemagick.spi;

import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Support functions for the Spi implementations. */
class SpiCommon {
    /** Vendor name. */
    static final String VENDOR_NAME = "de.sfuhrm.imagemagick2imageio";

    /** Version. */
    static final String VERSION = "1.0.0";

    /** The buffer size for reading image data. */
    private static final int BUFFER_SIZE = 16 * 1024;
    static byte[] readFully(ImageInputStream stream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        do {
            len = stream.read(buffer);
            if (len > 0) {
                os.write(buffer, 0, len);
            }
        } while (len >= 0);
        return os.toByteArray();
    }

    private static String[] imageFormats;
    private synchronized static String[] getFormats() {
        if (imageFormats == null) {
            NativeMagick magick = NativeMagick.instance();
            try {
                Set<String> formatsSet = magick.queryFormats();
                String[] lowerCase = formatsSet.stream().map(String::toLowerCase).toArray(String[]::new);
                imageFormats = lowerCase;
            } catch (MagickException e) {
                throw new RuntimeException(e);
            }
        }
        return imageFormats;
    }

    /** Returns a lower-case list of suffixes/formats we can handle.
     * */
    static String[] getSuffixes() {
        return getFormats();
    }

    /** Returns a lower-case list of mime types we can handle.
     * */
    static String[] getMimeTypes() {
        String[] formats = getFormats();
        return Arrays.stream(formats).map(s -> "image/" + s).toArray(String[]::new);
    }
}
