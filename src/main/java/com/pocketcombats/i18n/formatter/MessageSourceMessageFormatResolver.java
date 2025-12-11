package com.pocketcombats.i18n.formatter;

import com.ibm.icu.text.MessageFormat;
import com.pocketcombats.i18n.msgsource.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageSourceMessageFormatResolver implements MessageFormatResolver {

    private final MessageSource messageSource;
    private final Locale locale;

    private final Map<String, List<MessageFormat>> messageFormatCache = new ConcurrentHashMap<>();

    public MessageSourceMessageFormatResolver(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public Set<String> getMessageCodes() {
        return messageSource.getMessageCodes();
    }

    @Override
    public List<String> getMessagesFromSource(String code) {
        return messageSource.getMessages(code);
    }

    @Override
    public List<MessageFormat> getMessageFormatsForCode(String code) {
        return messageFormatCache.computeIfAbsent(
                code,
                (key) -> getMessagesFromSource(key)
                        .stream()
                        .map(message -> new MessageFormat(message, locale))
                        .toList()
        );
    }
}
