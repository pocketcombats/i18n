package com.pocketcombats.i18n.msgsource;

import java.util.List;
import java.util.Set;

/**
 * Source of raw localized message strings for a single locale.
 * <p>
 * Implementations should return an empty list when a code is unknown.
 * Variants of the same code may be represented by numeric suffixes
 * (e.g. {@code greeting}, {@code greeting.1}, {@code greeting.2}, up to {@code greeting.9}).
 */
public interface MessageSource {

    /**
     * @return set of base message codes (without numeric variant suffixes)
     */
    Set<String> getMessageCodes();

    /**
     * @param code base code
     * @return all variants for the given code; empty list if the code is unknown
     */
    List<String> getMessages(String code);
}
