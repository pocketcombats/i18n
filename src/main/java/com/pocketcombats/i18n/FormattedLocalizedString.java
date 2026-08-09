package com.pocketcombats.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ibm.icu.text.MessageFormat;
import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A more complex version of LocalizedString, which requires not only localization
 * but also formatting with arguments.
 * <p>
 * Can accept other LocalizedStrings in the arguments, in which case
 * it must localize/format them too.
 */
@JsonTypeName("formatted")
public final class FormattedLocalizedString extends LocalizedString {

    private final String code;
    private final Map<String, Object> args;
    private final int seed;

    @JsonCreator
    public FormattedLocalizedString(
            @JsonProperty("code") String code,
            @JsonProperty("args") Map<String, Object> args,
            @JsonProperty("seed") int seed
    ) {
        this.code = code;
        this.args = args;
        this.seed = seed;
    }

    @Override
    public String getMessage(MessageFormatResolver messageFormatResolver) {
        return super.getMessage(messageFormatResolver);
    }

    @Override
    void appendMessage(MessageFormatResolver messageFormatResolver, StringBuffer out) {
        if (args.isEmpty()) {
            List<String> messages = messageFormatResolver.getMessagesFromSource(code);
            if (messages.isEmpty()) {
                throw new IllegalArgumentException("No message for code " + code);
            }
            out.append(messages.get(seed % messages.size()));
        } else {
            Map<String, Object> formattedArgs = getFormattedArgs(messageFormatResolver);
            List<MessageFormat> messageFormats = messageFormatResolver.getMessageFormatsForCode(code);
            if (messageFormats.isEmpty()) {
                throw new IllegalArgumentException("No message for code " + code);
            }
            MessageFormat messageFormat = messageFormats.get(seed % messageFormats.size());
            if (!allArgsAreStrings(formattedArgs)) {
                // MessageFormat is not thread-safe but supports cloning
                messageFormat = messageFormat.clone();
            }
            // Formats straight into the tree's buffer
            messageFormat.format(formattedArgs, out, null);
        }
    }

    /**
     * @return localized string args, but having replaced all occurrences
     * of nested FormattedLocalizedStrings with plain formatted strings via the formatter
     */
    private Map<String, Object> getFormattedArgs(MessageFormatResolver messageFormatResolver) {
        if (!containsFormattedLocalizedString(args)) {
            // avoid args making copy if not needed
            return args;
        }
        Map<String, Object> mutableArgsCopy = new HashMap<>(args);
        for (Map.Entry<String, Object> argsEntry : mutableArgsCopy.entrySet()) {
            if (argsEntry.getValue() instanceof LocalizedString nestedLocalizedString) {
                String nestedFormattedMessage = nestedLocalizedString.getMessage(
                        messageFormatResolver
                );
                argsEntry.setValue(nestedFormattedMessage);
            }
        }
        return mutableArgsCopy;
    }

    public String getCode() {
        return code;
    }

    /**
     * Please match {@link LocalizedString} {@code JsonTypeInfo} configuration.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$")
    public Map<String, ?> getArgs() {
        return args;
    }

    public int getSeed() {
        return seed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .append("args", args)
                .append("seed", seed)
                .toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;


        if (o == null || getClass() != o.getClass()) return false;

        FormattedLocalizedString that = (FormattedLocalizedString) o;

        return new EqualsBuilder()
                .append(code, that.code)
                .append(args, that.args)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(code)
                .append(args)
                .toHashCode();
    }

    private static boolean containsFormattedLocalizedString(Map<String, Object> args) {
        for (Object value : args.values()) {
            if (value instanceof LocalizedString) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the cached {@link MessageFormat} for this code can be shared instead of cloned.
     * <p>
     * Formatting mutates a MessageFormat only where it lazily creates a number formatter, a date
     * formatter, or a plural/ordinal selector.
     * Nested {@link LocalizedString} arguments have already been resolved to Strings by
     * {@link #getFormattedArgs} before this check runs.
     */
    private static boolean allArgsAreStrings(Map<String, Object> args) {
        for (Object value : args.values()) {
            if (!(value instanceof String)) {
                return false;
            }
        }
        return true;
    }
}
