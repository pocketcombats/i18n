package com.pocketcombats.i18n.spring;

import com.pocketcombats.i18n.I18nService;
import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import com.pocketcombats.i18n.formatter.MessageFormatResolverBundle;
import com.pocketcombats.i18n.formatter.MessageFormatResolverFactory;
import com.pocketcombats.i18n.jackson.I18nModule;
import com.pocketcombats.i18n.jackson.LocalizedStringJacksonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@AutoConfiguration
@ComponentScan(basePackages = "com.pocketcombats.i18n")
@EnableConfigurationProperties(I18nProperties.class)
public class I18nConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageFormatResolverBundle messageBundle(I18nProperties i18nProperties) {
        Map<Locale, MessageFormatResolver> localeToSource = i18nProperties.localeToMessages().entrySet().stream()
                .collect(Collectors.toMap(
                        locale2path -> Locale.forLanguageTag(locale2path.getKey()),
                        locale2path -> createMessageFormatter(locale2path.getValue(), Locale.forLanguageTag(locale2path.getKey()))
                ));
        return new MessageFormatResolverBundle(localeToSource);
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
                Locale.forLanguageTag(i18nProperties.defaultLocale()),
                LocaleContextHolder::getLocale,
                i18nProperties.debugMode()
        );
    }

    @Bean
    public I18nModule jacksonModule(I18nService i18nService) {
        return I18nModule.forSerializer(new LocalizedStringJacksonSerializer(i18nService));
    }
}
