package com.pocketcombats.i18n.msgsource;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Loads messages from a classpath {@code .properties} file encoded in UTF-8.
 * <p>
 * Base codes are the keys without a numeric suffix. Additional variants are read
 * from keys with suffixes {@code .1}, {@code .2}, ... up to {@code .9}.
 * Unknown codes yield an empty list.
 */
public class PropertiesFileMessageSource implements MessageSource {

    private static final Pattern VARIANT_PATTERN = Pattern.compile("\\.\\d+$");

    private final Properties properties;
    private final Set<String> codes;

    private PropertiesFileMessageSource(Properties properties) {
        this.properties = properties;
        this.codes = properties.stringPropertyNames().stream()
                .filter(VARIANT_PATTERN.asPredicate().negate())
                .collect(Collectors.toSet());
    }

    /**
     * @param propertiesFilePath classpath-relative path to a UTF-8 properties file
     */
    public PropertiesFileMessageSource(String propertiesFilePath) {
        this(loadProperties(propertiesFilePath));
    }

    /**
     * @param resource Spring {@link Resource} pointing to a UTF-8 properties file
     */
    public PropertiesFileMessageSource(Resource resource) {
        this(loadProperties(resource));
    }

    private static Properties loadProperties(String filePath) {
        try (
                InputStream inputStream = getResourceAsStream(filePath);
                InputStreamReader inputReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        ) {
            return readProperties(inputReader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties from " + filePath);
        }
    }

    private static Properties loadProperties(Resource resource) {
        try (
                InputStream inputStream = resource.getInputStream();
                InputStreamReader inputReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        ) {
            return readProperties(inputReader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties from " + resource);
        }
    }

    private static Properties readProperties(InputStreamReader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    private static InputStream getResourceAsStream(String filePath) throws IOException {
        ClassLoader classLoader = PropertiesFileMessageSource.class.getClassLoader();
        if (classLoader == null) {
            throw new IOException("Can't read resource, classLoader is null");
        }
        URL resource = classLoader.getResource(filePath);
        if (resource == null) {
            throw new IOException("Can't read resource: " + filePath);
        }
        return resource.openStream();
    }

    @Override
    public Set<String> getMessageCodes() {
        return codes;
    }

    @Override
    public List<String> getMessages(String code) {
        String message = properties.getProperty(code);
        if (message == null) {
            return Collections.emptyList();
        }
        List<String> messages = new ArrayList<>();
        messages.add(message);
        for (int i = 1; i <= 10; i++) {
            message = properties.getProperty(code + "." + i);
            if (message == null) {
                break;
            } else {
                messages.add(message);
            }
        }
        return messages;
    }
}
