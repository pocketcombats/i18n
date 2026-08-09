package com.pocketcombats.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Simple case of localized string which must be translated/formatted,
 * but without additional arguments provided.
 */
@JsonTypeName("simple")
public final class SimpleLocalizedString extends LocalizedString {

    private final String code;
    private final int seed;

    @JsonCreator
    public SimpleLocalizedString(
            @JsonProperty("code") String code,
            @JsonProperty("seed") int seed
    ) {
        this.code = code;
        this.seed = seed;
    }

    @Override
    public String getMessage(MessageFormatResolver messageFormatResolver) {
        List<String> messages = messageFormatResolver.getMessagesFromSource(code);
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("No message for code " + code);
        }
        return messages.get(seed % messages.size());
    }

    @Override
    void appendMessage(MessageFormatResolver messageFormatResolver, StringBuffer out) {
        out.append(getMessage(messageFormatResolver));
    }

    public String getCode() {
        return code;
    }

    public int getSeed() {
        return seed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .append("seed", seed)
                .toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SimpleLocalizedString that = (SimpleLocalizedString) o;

        return new EqualsBuilder()
                .append(code, that.code)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(code)
                .toHashCode();
    }
}
