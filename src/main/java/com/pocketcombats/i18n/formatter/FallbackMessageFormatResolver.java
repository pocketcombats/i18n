package com.pocketcombats.i18n.formatter;

import com.ibm.icu.text.MessageFormat;

import java.util.List;
import java.util.Set;

/**
 * Delegates to a primary resolver and falls back to another resolver
 * when the primary returns no results for a code.
 */
public class FallbackMessageFormatResolver implements MessageFormatResolver {

    private final MessageFormatResolver delegate;
    private final MessageFormatResolver fallback;

    public FallbackMessageFormatResolver(MessageFormatResolver delegate, MessageFormatResolver fallback) {
        this.delegate = delegate;
        this.fallback = fallback;
    }

    @Override
    public Set<String> getMessageCodes() {
        return delegate.getMessageCodes();
    }

    @Override
    public List<String> getMessagesFromSource(String code) {
        List<String> messages = delegate.getMessagesFromSource(code);
        if (messages.isEmpty()) {
            return fallback.getMessagesFromSource(code);
        } else {
            return messages;
        }
    }

    @Override
    public List<MessageFormat> getMessageFormatsForCode(String code) {
        List<MessageFormat> messageFormats = delegate.getMessageFormatsForCode(code);
        if (messageFormats.isEmpty()) {
            return fallback.getMessageFormatsForCode(code);
        } else {
            return messageFormats;
        }
    }
}
