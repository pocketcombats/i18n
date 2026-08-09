package com.pocketcombats.i18n;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pocketcombats.i18n.formatter.MessageFormatResolver;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A serializable abstraction over a message that can be resolved to a human-readable string
 * given a {@link com.pocketcombats.i18n.formatter.MessageFormatResolver}.
 * <p>
 * Use the factory methods to create instances:
 * <ul>
 *     <li>{@link #direct(String)} — already localized literal, returned as is.</li>
 *     <li>{@link #simple(String)} — message code without arguments.</li>
 *     <li>{@link #formatted(String, Map)} — message code with arguments (ICU MessageFormat).</li>
 *     <li>{@link #joined(String, Collection)} — joins multiple localized strings with a delimiter.</li>
 * </ul>
 * For sources that define multiple variants for the same code, selection is stable within instance
 * via an internal seed.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$")
@JsonSubTypes({
        @JsonSubTypes.Type(DirectLocalizedString.class),
        @JsonSubTypes.Type(SimpleLocalizedString.class),
        @JsonSubTypes.Type(FormattedLocalizedString.class),
        @JsonSubTypes.Type(JoinedLocalizedString.class),
})
public abstract sealed class LocalizedString
        implements Serializable
        permits DirectLocalizedString, SimpleLocalizedString, FormattedLocalizedString, JoinedLocalizedString {

    /**
     * Resolves this message on its own.
     * Prefer {@link #appendMessage} when resolving into a buffer that already exists.
     */
    String getMessage(MessageFormatResolver messageFormatResolver) {
        StringBuffer out = new StringBuffer(64);
        appendMessage(messageFormatResolver, out);
        return out.toString();
    }

    /**
     * Appends this message, fully resolved, to {@code out}.
     * <p>
     * Resolving a tree of localized strings through one caller-supplied sink avoids
     * materializing an intermediate String for every node and copying it again into its parent.
     */
    abstract void appendMessage(MessageFormatResolver messageFormatResolver, StringBuffer out);

    /**
     * Creates a literal message that bypasses localization/formatting.
     */
    public static DirectLocalizedString direct(String message) {
        return new DirectLocalizedString(message);
    }

    /**
     * Creates a message identified by a code without arguments.
     */
    public static SimpleLocalizedString simple(String code) {
        return new SimpleLocalizedString(code, seed());
    }

    /**
     * Creates a message identified by a code with arguments for ICU {@code MessageFormat}.
     */
    public static FormattedLocalizedString formatted(String code, Map<String, Object> args) {
        return new FormattedLocalizedString(code, args, seed());
    }

    /**
     * Creates a message that joins provided localized strings using the delimiter.
     */
    public static JoinedLocalizedString joined(String delimiter, Collection<? extends LocalizedString> strings) {
        return new JoinedLocalizedString(delimiter, strings);
    }

    private static int seed() {
        return ThreadLocalRandom.current().nextInt(Integer.SIZE - 1);
    }
}
