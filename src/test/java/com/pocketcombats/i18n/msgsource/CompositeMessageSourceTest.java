package com.pocketcombats.i18n.msgsource;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompositeMessageSourceTest {

    private record MapMessageSource(Map<String, List<String>> messages) implements MessageSource {

        @Override
        public Set<String> getMessageCodes() {
            return messages.keySet();
        }

        @Override
        public List<String> getMessages(String code) {
            return messages.getOrDefault(code, List.of());
        }
    }

    private static MessageSource source(String... codeToMessage) {
        Map<String, List<String>> messages = new LinkedHashMap<>();
        for (int i = 0; i < codeToMessage.length; i += 2) {
            messages.put(codeToMessage[i], List.of(codeToMessage[i + 1]));
        }
        return new MapMessageSource(messages);
    }

    @Test
    void emptyDelegatesAreRejected() {
        assertThatThrownBy(() -> new CompositeMessageSource(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unknownCodeYieldsEmptyList() {
        MessageSource composite = new CompositeMessageSource(List.of(source("a", "A")));

        assertThat(composite.getMessages("no.such.code")).isEmpty();
    }

    @Test
    void earlierDelegateShadowsLaterOne() {
        MessageSource composite = new CompositeMessageSource(List.of(
                source("shared", "from first"),
                source("shared", "from second")
        ));

        assertThat(composite.getMessages("shared")).containsExactly("from first");
    }

    @Test
    void codesFromAllDelegatesAreReportedInDelegateOrder() {
        MessageSource composite = new CompositeMessageSource(List.of(
                source("a", "A", "b", "B"),
                source("c", "C")
        ));

        assertThat(composite.getMessageCodes()).containsExactly("a", "b", "c");
    }

    @Test
    void codeOnlyInLaterDelegateIsResolved() {
        MessageSource composite = new CompositeMessageSource(List.of(
                source("a", "A"),
                source("b", "B")
        ));

        assertThat(composite.getMessages("b")).containsExactly("B");
    }

    @Test
    void codeThatLooksLikeAVariantSuffixIsStillResolved() {
        // "greeting.1" is treated as a variant, so no delegate reports it from getMessageCodes(),
        // but a delegate resolves it when asked directly. Resolving it from a single source and
        // not from a composite would make behaviour depend on how many files are configured.
        MessageSource composite = new CompositeMessageSource(List.of(
                new PropertiesFileMessageSource("message_source_test.properties"),
                source("other", "Other")
        ));

        assertThat(composite.getMessageCodes()).doesNotContain("greeting.1");
        assertThat(composite.getMessages("greeting.1")).containsExactly("Hi");
    }
}
