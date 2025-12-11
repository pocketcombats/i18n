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

    public String getMessage(MessageFormatResolver messageFormatResolver) {
        if (args.isEmpty()) {
            List<String> messages = messageFormatResolver.getMessagesFromSource(code);
            return messages.get(seed % messages.size());
        } else {
            Map<String, Object> formattedArgs = getFormattedArgs(messageFormatResolver);
            List<MessageFormat> messageFormats = messageFormatResolver.getMessageFormatsForCode(code);
            MessageFormat messageFormat = messageFormats.get(seed % messageFormats.size());
            // MessageFormat is not thread-safe but supports cloning
            return messageFormat.clone().format(formattedArgs);
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

    private static boolean containsFormattedLocalizedString(Map<String, Object> args) {
        return args.values().stream()
                .anyMatch(it -> it instanceof LocalizedString);
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
                .append(seed, that.seed)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(code)
                .append(args)
                .append(seed)
                .toHashCode();
    }
}
