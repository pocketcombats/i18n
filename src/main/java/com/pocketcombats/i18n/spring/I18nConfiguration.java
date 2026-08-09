package com.pocketcombats.i18n.spring;

import com.pocketcombats.i18n.I18nService;
import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import com.pocketcombats.i18n.formatter.MessageFormatResolverBundle;
import com.pocketcombats.i18n.formatter.MessageFormatResolverFactory;
import com.pocketcombats.i18n.jackson.I18nModule;
import com.pocketcombats.i18n.jackson.LocalizedStringJacksonSerializer;
import com.pocketcombats.i18n.persistence.PersistenceAutoConfiguration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@AutoConfiguration(after = PersistenceAutoConfiguration.class)
@EnableConfigurationProperties(I18nProperties.class)
public class I18nConfiguration {

    private static final String LOCALE_TO_MESSAGES = "i18n.locale-to-messages";
    private static final String DEFAULT_LOCALE = "i18n.default-locale";

    @Bean
    @ConditionalOnMissingBean
    public MessageFormatResolverBundle messageBundle(I18nProperties i18nProperties) {
        Map<String, String> localeToMessages = i18nProperties.localeToMessages();
        if (localeToMessages == null || localeToMessages.isEmpty()) {
            throw new IllegalStateException(LOCALE_TO_MESSAGES + " must configure at least one locale");
        }
        Locale defaultLocale = parseLanguageTag(i18nProperties.defaultLocale(), DEFAULT_LOCALE);

        Map<Locale, MessageFormatResolver> localeToSource = localeToMessages.entrySet().stream()
                .map(locale2path -> Map.entry(
                        parseLanguageTag(locale2path.getKey(), LOCALE_TO_MESSAGES + '.' + locale2path.getKey()),
                        locale2path.getValue()
                ))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        locale2path -> createMessageFormatter(locale2path.getValue(), locale2path.getKey()),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    LOCALE_TO_MESSAGES + " must not contain two entries that resolve"
                                            + " to the same locale");
                        },
                        LinkedHashMap::new
                ));

        if (!localeToSource.containsKey(defaultLocale)) {
            throw new IllegalStateException(
                    DEFAULT_LOCALE + "=\"" + i18nProperties.defaultLocale() + "\" has no entry in "
                            + LOCALE_TO_MESSAGES + ", so messages missing from a locale could not"
                            + " fall back to anything. Configured locales: " + localeToSource.keySet()
            );
        }

        return new MessageFormatResolverBundle(localeToSource, defaultLocale);
    }

    /**
     * Rejects tags that {@link Locale#forLanguageTag} cannot make sense of.
     * Such a tag yields {@link Locale#ROOT}, which no user locale ever matches, so the entry silently
     * does nothing. The usual cause is the underscore form that {@link Locale#toString()} prints.
     */
    private static Locale parseLanguageTag(@Nullable String languageTag, String property) {
        if (languageTag == null || languageTag.isBlank()) {
            throw new IllegalStateException(
                    property + " must be set to a language tag such as \"en\" or \"en-US\"");
        }
        Locale locale = Locale.forLanguageTag(languageTag);
        if (Locale.ROOT.equals(locale)) {
            throw new IllegalStateException(
                    property + "=\"" + languageTag + "\" is not a valid language tag."
                            + " Use the BCP 47 form with a hyphen, such as \"en\" or \"en-US\"");
        }
        return locale;
    }

    private MessageFormatResolver createMessageFormatter(String messageFilePath, Locale locale) {
        return MessageFormatResolverFactory.create(messageFilePath, locale);
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nService i18nService(
            I18nProperties i18nProperties,
            MessageFormatResolverBundle messageFormatResolverBundle
    ) {
        return new I18nService(
                messageFormatResolverBundle,
                LocaleContextHolder::getLocale,
                i18nProperties.debugMode()
        );
    }

    @Bean
    public I18nModule jacksonModule(I18nService i18nService) {
        return I18nModule.forSerializer(new LocalizedStringJacksonSerializer(i18nService));
    }
}
