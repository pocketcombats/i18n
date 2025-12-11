package com.pocketcombats.i18n.formatter;

import com.ibm.icu.text.MessageFormat;

import java.util.List;
import java.util.Set;

/**
 * Resolves raw messages and compiled ICU {@link com.ibm.icu.text.MessageFormat} instances
 * for a particular locale.
 */
public interface MessageFormatResolver {

    /**
     * @return set of available message codes for this resolver; may be empty
     */
    Set<String> getMessageCodes();

    /**
     * @return all message variants for the given code in source order; empty list if none
     */
    List<String> getMessagesFromSource(String code);

    /**
     * @return compiled MessageFormat instances for all variants of the given code; empty list if none
     */
    List<MessageFormat> getMessageFormatsForCode(String code);
}
