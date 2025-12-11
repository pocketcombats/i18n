package com.pocketcombats.i18n.msgsource;

import java.util.List;
import java.util.stream.Collectors;

public class MessageSourceFactory {

    public static MessageSource create(List<String> messageFilePaths) {
        if (messageFilePaths.isEmpty()) {
            throw new IllegalArgumentException("No message source paths provided");
        }
        List<MessageSource> delegates = messageFilePaths.stream()
                .map(path -> createSingle(path.trim()))
                .collect(Collectors.toList());

        if (delegates.size() == 1) {
            return delegates.get(0);
        }
        return new CompositeMessageSource(delegates);
    }

    private static MessageSource createSingle(String messageFilePath) {
        if (messageFilePath.endsWith(".properties")) {
            return new PropertiesFileMessageSource(messageFilePath);
        }
        throw new IllegalArgumentException("Unsupported extension: " + messageFilePath);
    }
}
