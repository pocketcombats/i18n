package com.pocketcombats.i18n.spring;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

/**
 * @param localeToMessages message file paths per BCP 47 language tag, e.g., {@code en} or
 *                         {@code en-US}. Several paths may be separated by {@code ;}, and each
 *                         may contain wildcards
 * @param defaultLocale    language tag to fall back to. Required, and must have an entry in
 *                         {@code localeToMessages}
 * @param debugMode        when {@code true}, messages are not resolved and the raw
 *                         {@code LocalizedString} is rendered instead
 */
@ConfigurationProperties(prefix = "i18n")
public record I18nProperties(
        // both required properties are declared `@Nullable` because binding cannot enforce their presence
        @Nullable Map<String, String> localeToMessages,
        @Nullable String defaultLocale,
        @DefaultValue("false") boolean debugMode
) {
}
