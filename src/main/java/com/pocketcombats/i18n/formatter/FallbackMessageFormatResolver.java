package com.pocketcombats.i18n.formatter;

import com.ibm.icu.text.MessageFormat;

import java.util.Collections;
import java.util.LinkedHashSet;
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

    /**
     * Computed on call rather than in the constructor: this is a cold path used only by callers
     * that want the whole code set, so there is no point building the union for every configured
     * locale up front when most of them will never be asked for it.
     *
     * @return every code this resolver can resolve, i.e., the delegate codes followed by
     * the fallback-only ones, matching the order in which they would be looked up
     */
    @Override
    public Set<String> getMessageCodes() {
        // Use LinkedHashSet to keep predictable iteration order
        Set<String> union = new LinkedHashSet<>(delegate.getMessageCodes());
        union.addAll(fallback.getMessageCodes());
        return Collections.unmodifiableSet(union);
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
