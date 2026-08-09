package com.pocketcombats.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@TestPropertySource("classpath:application-test.properties")
class I18nServiceIT {

    @Autowired
    I18nService i18nService;

    @BeforeEach
    void setup() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
    }

    @Test
    void testDirectLocalizedString() {
        String message = "This message should not be tempered with";
        LocalizedString direct = LocalizedString.direct(message);
        assertThat(i18nService.getMessage(direct)).isEqualTo(message);
    }

    @Test
    void testSimpleLocalizedString() {
        LocalizedString simple = LocalizedString.simple("character.janny.name");
        assertThat(i18nService.getMessage(simple)).isEqualTo("Janny");
    }

    @Test
    void testFormattedLocalizedString() {
        FormattedLocalizedString formatted = LocalizedString.formatted(
                "character.janny.message.do_not_fear_you",
                Map.of("WHO", "Scrub")
        );
        assertThat(i18nService.getMessage(formatted)).isEqualTo("I do not fear you, Scrub");
    }

    @Test
    void testSimpleNestedFormattedLocalizedString() {
        LocalizedString localizedString = LocalizedString.formatted(
                "testSimpleNestedLocalizedString",
                Map.of(
                        "WHO", LocalizedString.simple("character.janny.name")
                )
        );

        assertThat(i18nService.getMessage(localizedString))
                .isEqualTo("Janny makes a vicious attack.");
    }

    @Test
    void testDeeplyNestedFormattedLocalizedString() {
        LocalizedString localizedString = LocalizedString.formatted(
                "testDeeplyNestedLocalizedString",
                Map.of(
                        "WHO", LocalizedString.simple("character.janny.name"),
                        "MESSAGE", LocalizedString.formatted(
                                "character.janny.message.do_not_fear_you",
                                Map.of("WHO", LocalizedString.simple("character.archibald.name"))
                        )
                )
        );

        assertThat(i18nService.getMessage(localizedString))
                .isEqualTo("Janny makes a vicious attack and has this to say: I do not fear you, Archibald!");
    }

    @Test
    void multiplePropertyFiles() {
        LocalizedString localizedString = LocalizedString.simple("multiple.property.files.key");
        assertThat(i18nService.getMessage(localizedString))
                .isEqualTo("Message from the second property file");
    }

    @Test
    void ruPropertySource() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));
        LocalizedString localizedString = LocalizedString.simple("character.archibald.name");
        assertThat(i18nService.getMessage(localizedString))
                .isEqualTo("Арчибальд");
    }

    @Test
    void fallbackToDefaultLocale() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));
        LocalizedString localizedString = LocalizedString.simple("untranslated.key");
        assertThat(i18nService.getMessage(localizedString))
                .isEqualTo("This string has no translation");
    }

    @Test
    void fallbackToDefaultLocaleForCodeMissingFromEveryFile() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));
        LocalizedString localizedString = LocalizedString.formatted(
                "character.janny.message.do_not_fear_you",
                Map.of("WHO", "Scrub")
        );
        assertThat(i18nService.getMessage(localizedString)).isEqualTo("I do not fear you, Scrub");
    }

    @Test
    void messageCodesIncludeUntranslatedDefaultLocaleCodes() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));
        assertThat(i18nService.getMessageCodes())
                .contains("character.archibald.name", "character.janny.name")
                .contains("untranslated.key", "multiple.property.files.key");
    }

    @Test
    void joinedLocalizedString() {
        LocalizedString joined = LocalizedString.joined(", ", List.of(
                LocalizedString.simple("character.janny.name"),
                LocalizedString.simple("character.archibald.name")
        ));
        assertThat(i18nService.getMessage(joined)).isEqualTo("Janny, Archibald");
    }

    @Test
    void joinedSingleElementHasNoDelimiter() {
        LocalizedString joined = LocalizedString.joined(", ", List.of(
                LocalizedString.simple("character.janny.name")
        ));
        assertThat(i18nService.getMessage(joined)).isEqualTo("Janny");
    }

    @Test
    void joinedEmptyYieldsEmptyString() {
        LocalizedString joined = LocalizedString.joined(", ", List.of());
        assertThat(i18nService.getMessage(joined)).isEmpty();
    }

    @Test
    void joinedOfFormattedWithNestedAndPlainArgs() {
        LocalizedString joined = LocalizedString.joined(" || ", List.of(
                LocalizedString.formatted(
                        "character.janny.message.do_not_fear_you",
                        Map.of("WHO", LocalizedString.simple("character.archibald.name"))
                ),
                LocalizedString.formatted(
                        "character.janny.message.do_not_fear_you",
                        Map.of("WHO", "Scrub")
                )
        ));
        assertThat(i18nService.getMessage(joined))
                .isEqualTo("I do not fear you, Archibald || I do not fear you, Scrub");
    }

    @Test
    void numericArgumentIsFormattedByIcu() {
        // Non-String args take the cloning path and must keep ICU's locale-aware number formatting
        // rather than String.valueOf.
        LocalizedString localizedString = LocalizedString.formatted(
                "character.janny.message.do_not_fear_you",
                Map.of("WHO", 1234567)
        );
        assertThat(i18nService.getMessage(localizedString)).isEqualTo("I do not fear you, 1,234,567");
    }

    @Test
    void everyReportedMessageCodeIsResolvable() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));
        assertThat(i18nService.getMessageCodes())
                .isNotEmpty()
                .allSatisfy(code ->
                        assertThat(i18nService.getMessage(LocalizedString.simple(code))).isNotNull()
                );
    }
}
