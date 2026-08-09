package com.pocketcombats.i18n.formatter;

import com.pocketcombats.i18n.msgsource.MessageSource;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FallbackMessageFormatResolverTest {

    private static final Locale RU = Locale.forLanguageTag("ru");
    private static final Locale EN = Locale.forLanguageTag("en");

    @Test
    void messageCodesAreTheUnionOfDelegateAndFallback() {
        MessageFormatResolver fallbackResolver = new FallbackMessageFormatResolver(
                resolver(RU, "shared.key", "ru.only.key"),
                resolver(EN, "shared.key", "en.only.key")
        );

        assertThat(fallbackResolver.getMessageCodes())
                .containsExactly("shared.key", "ru.only.key", "en.only.key");
    }

    @Test
    void everyReportedCodeIsResolvable() {
        MessageFormatResolver fallbackResolver = new FallbackMessageFormatResolver(
                resolver(RU, "shared.key", "ru.only.key"),
                resolver(EN, "shared.key", "en.only.key")
        );

        assertThat(fallbackResolver.getMessageCodes())
                .allSatisfy(code -> assertThat(fallbackResolver.getMessagesFromSource(code)).isNotEmpty());
    }

    @Test
    void delegateWinsOverFallbackForSharedCode() {
        MessageFormatResolver fallbackResolver = new FallbackMessageFormatResolver(
                resolver(RU, "shared.key"),
                resolver(EN, "shared.key")
        );

        assertThat(fallbackResolver.getMessagesFromSource("shared.key")).containsExactly("shared.key@ru");
    }

    @Test
    void fallbackOnlyCodeIsResolvedFromFallback() {
        MessageFormatResolver fallbackResolver = new FallbackMessageFormatResolver(
                resolver(RU, "ru.only.key"),
                resolver(EN, "en.only.key")
        );

        assertThat(fallbackResolver.getMessagesFromSource("en.only.key")).containsExactly("en.only.key@en");
    }

    @Test
    void unknownCodeStaysEmpty() {
        MessageFormatResolver fallbackResolver = new FallbackMessageFormatResolver(
                resolver(RU, "ru.only.key"),
                resolver(EN, "en.only.key")
        );

        assertThat(fallbackResolver.getMessagesFromSource("missing.key")).isEmpty();
        assertThat(fallbackResolver.getMessageFormatsForCode("missing.key")).isEmpty();
        assertThat(fallbackResolver.getMessageCodes()).doesNotContain("missing.key");
    }

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

    private static MessageFormatResolver resolver(Locale locale, String... codes) {
        Map<String, List<String>> messages = new LinkedHashMap<>();
        for (String code : codes) {
            messages.put(code, List.of(code + "@" + locale.getLanguage()));
        }
        return new MessageSourceMessageFormatResolver(new MapMessageSource(messages), locale);
    }
}
