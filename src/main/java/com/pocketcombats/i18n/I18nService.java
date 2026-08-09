package com.pocketcombats.i18n;

import com.pocketcombats.i18n.formatter.MessageFormatResolver;
import com.pocketcombats.i18n.formatter.MessageFormatResolverBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Entry point for resolving {@link LocalizedString} to the final message for the current user locale.
 * <p>
 * Resolution uses a {@link MessageFormatResolverBundle} to pick a resolver for the user locale
 * with fallbacks configured by the bundle. When {@code isDebugMode} is enabled, the service
 * does not resolve messages and instead returns {@link LocalizedString#toString()} for easier debugging.
 */
public class I18nService {

    private static final Log LOG = LogFactory.getLog(I18nService.class);

    private final MessageFormatResolverBundle messageSourceBundle;
    private final Supplier<Locale> localeSupplier;
    private final boolean isDebugMode;

    /**
     * @param messageSourceBundle resolvers for locales and their fallbacks, including the default locale
     *                            the bundle falls back to
     * @param localeSupplier      supplies user locale at call time
     * @param isDebugMode         when {@code true}, messages are not resolved and {@code toString()} is returned
     */
    public I18nService(
            MessageFormatResolverBundle messageSourceBundle,
            Supplier<Locale> localeSupplier,
            boolean isDebugMode
    ) {
        this.messageSourceBundle = messageSourceBundle;
        this.localeSupplier = localeSupplier;
        this.isDebugMode = isDebugMode;
    }

    /**
     * Resolves the given {@link LocalizedString} to a message for the current user locale.
     * <ul>
     *   <li>If debug mode is enabled, returns {@code localizedString.toString()}.</li>
     *   <li>If no resolver can be found for the user/default locale configuration, returns {@code null}.</li>
     * </ul>
     */
    public @Nullable String getMessage(LocalizedString localizedString) {
        if (isDebugMode) {
            return localizedString.toString();
        }
        if (localizedString instanceof DirectLocalizedString directLocalizedString) {
            // small optimization. since we know that direct localized strings do not need translation,
            // we can return straight away, without having to extract locale and look for the formatter
            return directLocalizedString.getMessage(null);
        }

        Locale userLocale = localeSupplier.get();
        MessageFormatResolver messageFormatResolver = resolveFormatResolver(userLocale);
        if (messageFormatResolver == null) {
            return null;
        }

        // One buffer for the whole tree
        StringBuffer out = new StringBuffer(64);
        localizedString.appendMessage(messageFormatResolver, out);
        return out.toString();
    }

    /**
     * Returns all available message codes for the current user locale based on the selected resolver.
     * Returns an empty set when no resolver can be found.
     */
    public Set<String> getMessageCodes() {
        Locale userLocale = localeSupplier.get();
        MessageFormatResolver messageFormatResolver = resolveFormatResolver(userLocale);
        if (messageFormatResolver == null) {
            return Collections.emptySet();
        } else {
            return messageFormatResolver.getMessageCodes();
        }
    }

    private @Nullable MessageFormatResolver resolveFormatResolver(Locale userLocale) {
        MessageFormatResolver messageFormatResolver = messageSourceBundle.getForLocale(userLocale);
        if (messageFormatResolver == null) {
            LOG.error("Unable to find message formatter for locale=" + userLocale);
            return null;
        } else {
            return messageFormatResolver;
        }
    }
}
