package com.pocketcombats.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@TestPropertySource("classpath:application-wildcard-test.properties")
class WildcardMessageSourceIT {

    @Autowired
    I18nService i18nService;

    @BeforeEach
    void setup() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
    }

    @Test
    void wildcardMatchesFirstFile() {
        LocalizedString localizedString = LocalizedString.simple("character.janny.name");
        assertThat(i18nService.getMessage(localizedString)).isEqualTo("Janny");
    }

    @Test
    void wildcardMatchesSecondFile() {
        LocalizedString localizedString = LocalizedString.simple("multiple.property.files.key");
        assertThat(i18nService.getMessage(localizedString))
                .isEqualTo("Message from the second property file");
    }
}
