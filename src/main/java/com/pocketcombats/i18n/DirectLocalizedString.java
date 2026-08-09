package com.pocketcombats.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Does not require formatting/localization, must be returned/processed as is.
 */
@JsonTypeName("direct")
public final class DirectLocalizedString extends LocalizedString {

    private final String message;

    @JsonCreator
    public DirectLocalizedString(@JsonProperty("message") String message) {
        this.message = message;
    }

    /**
     * @param messageFormatResolver unused
     */
    @Override
    public String getMessage(@Nullable MessageFormatResolver messageFormatResolver) {
        return message;
    }

    public String getMessage() {
        return message;
    }

    /**
     * @param messageFormatResolver unused
     */
    @Override
    void appendMessage(@Nullable MessageFormatResolver messageFormatResolver, StringBuffer out) {
        out.append(message);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DirectLocalizedString that = (DirectLocalizedString) o;

        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(message).toHashCode();
    }
}
