package com.pocketcombats.i18n.msgsource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CompositeMessageSource implements MessageSource {

    private final List<MessageSource> delegates;
    private final Set<String> codes;

    public CompositeMessageSource(List<MessageSource> delegates) {
        if (delegates.isEmpty()) {
            throw new IllegalArgumentException("Delegates must not be null or empty");
        }
        this.delegates = List.copyOf(delegates);
        // Use LinkedHashSet to keep predictable iteration order
        this.codes = delegates.stream()
                .flatMap(d -> d.getMessageCodes().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> getMessageCodes() {
        return codes;
    }

    @Override
    public List<String> getMessages(String code) {
        for (MessageSource delegate : delegates) {
            if (delegate.getMessageCodes().contains(code)) {
                return delegate.getMessages(code);
            }
        }
        throw new IllegalArgumentException("No message for code " + code);
    }
}
