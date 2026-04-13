package com.pocketcombats.i18n.msgsource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MessageSourceFactory {

    private static final PathMatchingResourcePatternResolver RESOURCE_RESOLVER =
            new PathMatchingResourcePatternResolver();

    public static MessageSource create(List<String> messageFilePaths) {
        if (messageFilePaths.isEmpty()) {
            throw new IllegalArgumentException("No message source paths provided");
        }
        List<MessageSource> delegates = messageFilePaths.stream()
                .flatMap(path -> resolvePatterns(path.trim()).stream())
                .toList();

        if (delegates.size() == 1) {
            return delegates.get(0);
        }
        return new CompositeMessageSource(delegates);
    }

    private static List<MessageSource> resolvePatterns(String messageFilePath) {
        if (messageFilePath.contains("*")) {
            return createFromWildcard(messageFilePath);
        } else {
            return List.of(createSingle(messageFilePath));
        }
    }

    private static MessageSource createSingle(String messageFilePath) {
        if (messageFilePath.endsWith(".properties")) {
            return new PropertiesFileMessageSource(messageFilePath);
        }
        throw new IllegalArgumentException("Unsupported extension: " + messageFilePath);
    }

    private static List<MessageSource> createFromWildcard(String messageFilePath) {
        Resource[] resources;
        try {
            resources = RESOURCE_RESOLVER.getResources("classpath*:" + messageFilePath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve wildcard pattern: " + messageFilePath, e);
        }
        if (resources.length == 0) {
            throw new IllegalArgumentException("No classpath resources matched the pattern: " + messageFilePath);
        }

        return Arrays.stream(resources)
                .<MessageSource>map(resource -> {
                    String filename = resource.getFilename();
                    if (filename == null) {
                        throw new IllegalArgumentException("Cannot determine filename for resource: " + resource);
                    }
                    return new PropertiesFileMessageSource(resource);
                })
                .toList();
    }
}
