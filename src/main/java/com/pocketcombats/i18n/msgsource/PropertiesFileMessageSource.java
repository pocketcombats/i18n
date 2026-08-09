package com.pocketcombats.i18n.msgsource;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * <p>
 * The file is read once and flattened into an immutable code-to-variants map, so resolving a
 * message is a single map lookup that allocates nothing.
 */
public class PropertiesFileMessageSource implements MessageSource {

    private static final Pattern VARIANT_PATTERN = Pattern.compile("\\.\\d+$");

    /**
     * Highest numeric suffix considered when collecting variants of a code.
     */
    private static final int MAX_VARIANT_SUFFIX = 9;

    private final Map<String, List<String>> messages;
    private final Set<String> codes;

    private PropertiesFileMessageSource(Properties properties) {
        this.messages = index(properties);
        this.codes = properties.stringPropertyNames().stream()
                .filter(VARIANT_PATTERN.asPredicate().negate())
                .collect(Collectors.toUnmodifiableSet());
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

    /**
     * Flattens the raw properties into a lookup of a key to its variant chain.
     */
    private static Map<String, List<String>> index(Properties properties) {
        Set<String> keys = properties.stringPropertyNames();
        Map<String, List<String>> index = new HashMap<>(keys.size());
        for (String key : keys) {
            List<String> variants = new ArrayList<>(1);
            variants.add(properties.getProperty(key));
            for (int i = 1; i <= MAX_VARIANT_SUFFIX; i++) {
                String variant = properties.getProperty(key + "." + i);
                if (variant == null) {
                    break;
                }
                variants.add(variant);
            }
            index.put(key, List.copyOf(variants));
        }
        return Map.copyOf(index);
    }

    private static Properties loadProperties(String filePath) {
        try (
                InputStream inputStream = getResourceAsStream(filePath);
                InputStreamReader inputReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        ) {
            return readProperties(inputReader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties from " + filePath, e);
        }
    }

    private static Properties loadProperties(Resource resource) {
        try (
                InputStream inputStream = resource.getInputStream();
                InputStreamReader inputReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        ) {
            return readProperties(inputReader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties from " + resource, e);
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
        return messages.getOrDefault(code, List.of());
    }
}
