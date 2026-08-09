package com.pocketcombats.i18n;

import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import com.pocketcombats.i18n.formatter.MessageSourceMessageFormatResolver;
import com.pocketcombats.i18n.msgsource.MessageSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormattedLocalizedStringTest {

    private record MapMessageSource(Map<String, List<String>> messages) implements MessageSource {

        @Override
        public Set<String> getMessageCodes() {
            return messages.keySet();
        }

        @Override
        public List<String> getMessages(String code) {
            return messages.getOrDefault(code, List.of());
        }
    }

    private static MessageFormatResolver resolver() {
        return new MessageSourceMessageFormatResolver(
                new MapMessageSource(Map.of("known.code", List.of("Hello, {WHO}"))),
                Locale.ENGLISH
        );
    }

    @Test
    void unknownCodeWithArgumentsReportsTheMissingCode() {
        FormattedLocalizedString localizedString = LocalizedString.formatted(
                "unknown.code",
                Map.of("WHO", "Scrub")
        );

        assertThatThrownBy(() -> localizedString.getMessage(resolver()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No message for code unknown.code");
    }

    @Test
    void unknownCodeWithoutArgumentsReportsTheMissingCode() {
        FormattedLocalizedString localizedString = LocalizedString.formatted(
                "unknown.code",
                Map.of()
        );

        assertThatThrownBy(() -> localizedString.getMessage(resolver()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No message for code unknown.code");
    }

    @Test
    void knownCodeStillResolves() {
        FormattedLocalizedString localizedString = LocalizedString.formatted(
                "known.code",
                Map.of("WHO", "Scrub")
        );

        assertThat(localizedString.getMessage(resolver())).isEqualTo("Hello, Scrub");
    }
}
