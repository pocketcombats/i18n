package com.pocketcombats.i18n.formatter;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Holds resolvers per locale and provides locale selection with sensible fallbacks.
 */
public class MessageFormatResolverBundle {

    private final Map<Locale, MessageFormatResolver> byLocale;
    private final Map<String, MessageFormatResolver> byLanguage;
    private final @Nullable MessageFormatResolver defaultResolver;

    /**
     * @param messageSources resolver for each configured locale
     * @param defaultLocale  locale to fall back to for locales and codes that are not configured.
     *                       Held by the bundle rather than passed per call so that the fallback
     *                       wiring can be prepared once, and so that it cannot vary between calls
     */
    public MessageFormatResolverBundle(
            Map<Locale, MessageFormatResolver> messageSources,
            Locale defaultLocale
    ) {
        MessageFormatResolver fallback = messageSources.get(defaultLocale);

        Map<Locale, MessageFormatResolver> byLocale = new HashMap<>();
        Map<String, MessageFormatResolver> byLanguage = new HashMap<>();
        messageSources.forEach((locale, resolver) -> {
            MessageFormatResolver selected = fallback != null && !locale.equals(defaultLocale)
                    ? new FallbackMessageFormatResolver(resolver, fallback)
                    : resolver;
            byLocale.put(locale, selected);
            if (locale.equals(Locale.forLanguageTag(locale.getLanguage()))) {
                byLanguage.put(locale.getLanguage(), selected);
            }
        });

        this.byLocale = Map.copyOf(byLocale);
        this.byLanguage = Map.copyOf(byLanguage);
        this.defaultResolver = fallback;
    }

    /**
     * Selects a resolver using the following strategy:
     * <ol>
     *   <li>Try exact {@code userLocale}.</li>
     *   <li>If not present, try the language-only locale derived from {@code userLocale}.</li>
     *   <li>A match that is not the default locale resolves through a fallback to the default,
     *       so codes it is missing still resolve.</li>
     *   <li>If nothing matches, use the default locale.</li>
     * </ol>
     *
     * @return selected resolver, or {@code null} if neither the user nor the default locale is configured
     */
    public @Nullable MessageFormatResolver getForLocale(Locale userLocale) {
        MessageFormatResolver resolver = byLocale.get(userLocale);
        if (resolver == null) {
            resolver = byLanguage.get(userLocale.getLanguage());
        }
        return resolver != null ? resolver : defaultResolver;
    }
}
