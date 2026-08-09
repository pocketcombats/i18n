package com.pocketcombats.i18n.spring;

import com.pocketcombats.i18n.I18nService;
import com.pocketcombats.i18n.formatter.MessageFormatResolverBundle;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Configuration mistakes must fail while the context is starting.
 */
class I18nConfigurationTest {

    private static final String EN = "icu4j_test_messages_en.properties";
    private static final String RU = "icu4j_test_messages_ru.properties";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(I18nConfiguration.class));

    @Test
    void validConfigurationStarts() {
        runner.withPropertyValues(
                "i18n.default-locale=en",
                "i18n.locale-to-messages.en=" + EN,
                "i18n.locale-to-messages.ru=" + RU
        ).run(context -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(MessageFormatResolverBundle.class)
                .hasSingleBean(I18nService.class));
    }

    @Test
    void autoConfigurationDoesNotRegisterBeansFromItsOwnPackage() {
        // Component scanning from an auto-configuration would register whatever an application
        // happens to place under com.pocketcombats.i18n. IntegrationTestConfiguration sits in
        // that package and serves as the canary: it must not be pulled into the context.
        runner.withPropertyValues(
                "i18n.default-locale=en",
                "i18n.locale-to-messages.en=" + EN
        ).run(context -> assertThat(context)
                .hasNotFailed()
                .doesNotHaveBean("integrationTestConfiguration")
                .doesNotHaveBean(JsonMapper.class));
    }

    @Test
    void regionQualifiedLanguageTagIsAccepted() {
        runner.withPropertyValues(
                "i18n.default-locale=en-US",
                "i18n.locale-to-messages.en-US=" + EN
        ).run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void missingDefaultLocaleIsRejected() {
        runner.withPropertyValues(
                "i18n.locale-to-messages.en=" + EN
        ).run(context -> assertThat(context)
                .getFailure()
                .rootCause()
                .hasMessageContaining("i18n.default-locale")
                .hasMessageContaining("must be set"));
    }

    @Test
    void underscoreFormDefaultLocaleIsRejected() {
        // Locale.toString() prints this form, so it is the mistake people actually make
        runner.withPropertyValues(
                "i18n.default-locale=en_US",
                "i18n.locale-to-messages.en=" + EN
        ).run(context -> assertThat(context)
                .getFailure()
                .rootCause()
                .hasMessageContaining("i18n.default-locale=\"en_US\"")
                .hasMessageContaining("not a valid language tag"));
    }

    @Test
    void underscoreFormMessageKeyIsRejected() {
        runner.withPropertyValues(
                "i18n.default-locale=en",
                "i18n.locale-to-messages.en=" + EN,
                "i18n.locale-to-messages.ru_RU=" + RU
        ).run(context -> assertThat(context)
                .getFailure()
                .rootCause()
                .hasMessageContaining("i18n.locale-to-messages.ru_RU")
                .hasMessageContaining("not a valid language tag"));
    }

    @Test
    void defaultLocaleWithoutItsOwnMessagesIsRejected() {
        runner.withPropertyValues(
                "i18n.default-locale=de",
                "i18n.locale-to-messages.en=" + EN
        ).run(context -> assertThat(context)
                .getFailure()
                .rootCause()
                .hasMessageContaining("i18n.default-locale=\"de\"")
                .hasMessageContaining("has no entry in i18n.locale-to-messages"));
    }

    @Test
    void emptyMessageConfigurationIsRejected() {
        runner.withPropertyValues(
                "i18n.default-locale=en"
        ).run(context -> assertThat(context)
                .getFailure()
                .rootCause()
                .hasMessageContaining("i18n.locale-to-messages")
                .hasMessageContaining("at least one locale"));
    }
}
