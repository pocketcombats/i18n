package com.pocketcombats.i18n.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

@ConfigurationProperties(prefix = "i18n")
public record I18nProperties(
        Map<String, String> localeToMessages,
        String defaultLocale,
        @DefaultValue("false") boolean debugMode
) {
}
