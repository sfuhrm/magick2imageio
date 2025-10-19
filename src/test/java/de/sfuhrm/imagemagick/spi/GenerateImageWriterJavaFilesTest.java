package de.sfuhrm.imagemagick.spi;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenerateImageWriterJavaFilesTest {

    String packageName = "de.sfuhrm.imagemagick.spi.generated";

    /** These are ImageMagick formats that at least render problems with Java naming conventions. */
    static final Pattern BLOCKED_FORMATS_PATTERN = Pattern.compile("(3FR|3G2|3GP|.*-.*)");

    /** Generates the SPI writer descriptor file {@code javax.imageio.spi.ImageWriterSpi}. */
    @Disabled
    @Test
    public void generateImageWriterSpiDescriptor() throws MagickException, IOException {
        NativeMagick instance = new NativeMagick();
        Set<String> formats = instance.queryFormats();
        List<String> classNames = formats.stream()
                .filter(format -> !BLOCKED_FORMATS_PATTERN.matcher(format).matches())
                .map(format ->
                    format.toUpperCase() + "ImageWriterSpi")
                .sorted()
                .toList();
        Map<String, Object> context = new HashMap<>();
        context.put("packageName", packageName);
        context.put("names", classNames);

        String spiFile = createTemplateInstance(context, "ImageWriterSpiDescriptor.vm");
        Path target = Paths.get("src/main/resources/META-INF/services/javax.imageio.spi.ImageWriterSpi");
        Files.writeString(target, spiFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        System.out.println(spiFile);
    }

    /** Generates all SPI writer java files for each image format. */
    @Disabled
    @Test
    public void generateImageWriterSpiFiles() throws MagickException, IOException {
        NativeMagick instance = new NativeMagick();
        Set<String> formats = instance.queryFormats();

        for (String magickName : formats) {
            if (BLOCKED_FORMATS_PATTERN.matcher(magickName).matches()) {
                continue;
            }
            String suffix = magickName.toLowerCase();
            String mimeType = "image/" + suffix;
            String writerSpiClassName = suffix.toUpperCase() + "ImageWriterSpi";
            String writerSpiSuperClassName = AbstractImageMagickImageWriterSpi.class.getName();
            String packagePath = "de/sfuhrm/imagemagick/spi/generated";

            Map<String, Object> context = new HashMap<>();
            context.put("magickName", magickName);
            context.put("suffix", suffix);
            context.put("mimeType", mimeType);
            context.put("writerSpiClassName", writerSpiClassName);
            context.put("writerSpiSuperClassName", writerSpiSuperClassName);
            context.put("packagePath", packagePath);
            context.put("packageName", packageName);

            String writerClass = createTemplateInstance(context, "WriterSpi.java.vm");

            Path target = Paths.get("src/main/java/" + packagePath + "/" + writerSpiClassName + ".java");
            if (Files.exists(target)) {
                Files.delete(target);
            }
            Files.writeString(target, writerClass, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            System.out.println(writerClass);
        }
    }

    /** Renders a Velocity template.
     * @param properties template parameters.
     * @param templateName template file name in classpath.
     * */
    private String createTemplateInstance(Map<String, Object> properties, String templateName) {
        Velocity.init();
        VelocityContext context = new VelocityContext(properties);
        StringWriter writer = new StringWriter();
        Reader reader = new InputStreamReader(getClass().getResourceAsStream("/" + templateName), StandardCharsets.UTF_8);
        Velocity.evaluate(context, writer, "Tag", reader);
        return writer.toString();
    }
}
