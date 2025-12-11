package com.pocketcombats.i18n.formatter;

import com.pocketcombats.i18n.msgsource.MessageSource;
import com.pocketcombats.i18n.msgsource.MessageSourceFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MessageFormatResolverFactory {

    public static MessageFormatResolver create(String messageFilePaths, Locale locale) {
        List<String> paths = Arrays.stream(messageFilePaths.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        MessageSource messageSource = MessageSourceFactory.create(paths);
        return new MessageSourceMessageFormatResolver(messageSource, locale);
    }
}
