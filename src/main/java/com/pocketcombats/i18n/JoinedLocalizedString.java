package com.pocketcombats.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@JsonTypeName("joined")
public final class JoinedLocalizedString extends LocalizedString {

    private final String delimiter;
    private final Collection<? extends LocalizedString> strings;

    @JsonCreator
    public JoinedLocalizedString(
            @JsonProperty("delimiter") String delimiter,
            @JsonProperty("strings") Collection<? extends LocalizedString> strings
    ) {
        this.delimiter = delimiter;
        this.strings = strings;
    }

    @Override
    String getMessage(MessageFormatResolver messageFormatResolver) {
        return strings.stream()
                .map(string -> string.getMessage(messageFormatResolver))
                .collect(Collectors.joining(delimiter));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        JoinedLocalizedString that = (JoinedLocalizedString) o;

        return new EqualsBuilder()
                .append(delimiter, that.delimiter)
                .append(strings, that.strings)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(delimiter)
                .append(strings)
                .toHashCode();
    }

    public String getDelimiter() {
        return delimiter;
    }

    public Collection<? extends LocalizedString> getStrings() {
        return new ArrayList<>(strings);
    }
}
