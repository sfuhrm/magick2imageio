package de.sfuhrm.imagemagick.spi;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenerateImageWriterJavaFilesTest {

    String packageName = "de.sfuhrm.imagemagick.spi.generated";

    @Test
    public void generateImageWriterSpiDescriptor() throws MagickException {
        Set<String> writerSpiNames = new TreeSet<>();
        NativeMagick instance = new NativeMagick();
        Set<String> formats = instance.queryFormats();
        Set<String> classNames = formats.stream()
                .map(format ->
                    format.toUpperCase() + "ImageWriterSpi")
                .collect(Collectors.toSet());
        Map<String, Object> context = new HashMap<>();
        context.put("packageName", packageName);
        context.put("names", classNames);

        String spiFile = createInstance(context, "ImageWriterSpiDescriptor.vm");
        Path 
        System.out.println(spiFile);
    }

    @Test
    public void generateImageWriterSpiFiles() throws MagickException {
        NativeMagick instance = new NativeMagick();
        Set<String> formats = instance.queryFormats();
        Set<String> writerSpiNames = new TreeSet<>();

        for (String magickName : formats) {
            String suffix = magickName.toLowerCase();
            String mimeType = "image/" + suffix;
            String writerSpiClassName = suffix.toUpperCase() + "ImageWriterSpi";
            String writerSpiSuperClassName = AbstractImageMagickImageWriterSpi.class.getName();
            String packagePath = "src/main/java/de/sfuhrm/imagemagick/spi/generated";

            writerSpiNames.add(packageName + writerSpiClassName);

            Map<String, Object> context = new HashMap<>();
            context.put("magickName", magickName);
            context.put("suffix", suffix);
            context.put("mimeType", mimeType);
            context.put("writerSpiClassName", writerSpiClassName);
            context.put("writerSpiSuperClassName", writerSpiSuperClassName);
            context.put("packagePath", packagePath);
            context.put("packageName", packageName);

            String writerClass = createInstance(context, "WriterSpi.java.vm");
            System.out.println(writerClass);
        }
    }

    private String createInstance(Map<String, ?> properties, String templateName) {
        Velocity.init();
        VelocityContext context = new VelocityContext(properties);
        StringWriter writer = new StringWriter();
        Reader reader = new InputStreamReader(getClass().getResourceAsStream("/" + templateName), StandardCharsets.UTF_8);
        Velocity.evaluate(context, writer, "Tag", reader);
        return writer.toString();
    }
}
