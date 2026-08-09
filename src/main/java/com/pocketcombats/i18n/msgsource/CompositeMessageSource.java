package com.pocketcombats.i18n.msgsource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combines several message sources for a single locale.
 * <p>
 * A code is resolved by the first delegate that declares it, so earlier delegates
 * shadow later ones.
 * Unknown codes yield an empty list.
 * <p>
 * Resolution is served from a merged view built once at construction, so looking a code up costs
 * a single map lookup no matter how many delegates there are.
 */
public class CompositeMessageSource implements MessageSource {

    private final List<MessageSource> delegates;
    private final Map<String, List<String>> messages;
    private final Set<String> codes;

    public CompositeMessageSource(List<MessageSource> delegates) {
        if (delegates.isEmpty()) {
            throw new IllegalArgumentException("Delegates must not be null or empty");
        }
        this.delegates = List.copyOf(delegates);
        Map<String, List<String>> messages = new LinkedHashMap<>();
        Set<String> codes = new LinkedHashSet<>();
        for (MessageSource delegate : this.delegates) {
            for (String code : delegate.getMessageCodes()) {
                codes.add(code);
                messages.putIfAbsent(code, delegate.getMessages(code));
            }
        }
        this.messages = Map.copyOf(messages);
        this.codes = Collections.unmodifiableSet(codes);
    }

    @Override
    public Set<String> getMessageCodes() {
        return codes;
    }

    @Override
    public List<String> getMessages(String code) {
        List<String> merged = messages.get(code);
        if (merged != null) {
            return merged;
        }
        for (MessageSource delegate : delegates) {
            List<String> fromDelegate = delegate.getMessages(code);
            if (!fromDelegate.isEmpty()) {
                return fromDelegate;
            }
        }
        return List.of();
    }
}
