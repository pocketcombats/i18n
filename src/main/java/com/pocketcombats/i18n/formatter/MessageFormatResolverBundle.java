package com.pocketcombats.i18n.formatter;


import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Holds resolvers per locale and provides locale selection with sensible fallbacks.
 */
public class MessageFormatResolverBundle {

    private final Map<Locale, MessageFormatResolver> messageSources;

    public MessageFormatResolverBundle(Map<Locale, MessageFormatResolver> messageSources) {
        this.messageSources = messageSources;
    }

    /**
     * Selects a resolver using the following strategy:
     * <ol>
     *   <li>Try exact {@code userLocale}.</li>
     *   <li>If not present, try language-only locale derived from {@code userLocale}.</li>
     *   <li>If found and different from {@code defaultLocale}, wrap it with a fallback to the default locale.</li>
     *   <li>If nothing found, try {@code defaultLocale}.</li>
     * </ol>
     * @return selected resolver or {@code null} if neither user nor default locale is configured
     */
    public @Nullable MessageFormatResolver getForLocale(Locale userLocale, Locale defaultLocale) {
        Locale selectedLocale = userLocale;
        MessageFormatResolver resolver;
        resolver = messageSources.get(selectedLocale);
        if (resolver == null) {
            selectedLocale = Locale.forLanguageTag(userLocale.getLanguage());
            if (!selectedLocale.equals(userLocale)) {
                resolver = messageSources.get(selectedLocale);
            }
        }
        if (resolver != null) {
            if (!selectedLocale.equals(defaultLocale)) {
                MessageFormatResolver fallback = messageSources.get(defaultLocale);
                if (fallback != null) {
                    resolver = new FallbackMessageFormatResolver(resolver, fallback);
                }
            }
        } else {
            resolver = messageSources.get(defaultLocale);
        }
        return resolver;
    }
}
