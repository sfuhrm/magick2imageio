package de.sfuhrm.imagemagick.spi;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageAsserts {
    public static void compareBufferedImages(BufferedImage expectedImage, BufferedImage actualImage, int acceptableDifference) {
        assertNotNull(expectedImage);
        assertEquals(expectedImage.getWidth(), actualImage.getWidth());
        assertEquals(expectedImage.getHeight(), actualImage.getHeight());

        long deltas = 0;
        for (int y = 0; y < expectedImage.getHeight(); y++) {
            for (int x = 0; x < expectedImage.getWidth(); x++) {
                int expected = expectedImage.getRGB(x, y);
                int actual = actualImage.getRGB(x, y);

                // ImageMagick always has around 1 difference compared
                // to ImageIO
                int maxDifference = 0;
                for (int i = 0; i < 4; i++) {
                    int expectedGun = 0xff & (expected >>> (3 * i));
                    int actualGun = 0xff & (actual >>> (3 * i));
                    int diff = Math.abs(expectedGun - actualGun);
                    maxDifference = Math.max(maxDifference, diff);
                    deltas += diff;
                }
                String expectedHex = Integer.toString(expected, 16);
                String actualHex = Integer.toString(actual, 16);

                assertTrue(maxDifference <= acceptableDifference, "Per-pixel-gun difference may not exceed " + acceptableDifference + ". "
                        + "Expected hex: #" + expectedHex
                        + ", Actual Hex: #" + actualHex);
            }
        }
        System.out.println("DeltaSum: " + deltas);
    }
}
