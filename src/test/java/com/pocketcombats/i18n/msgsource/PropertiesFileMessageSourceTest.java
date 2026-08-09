package com.pocketcombats.i18n.msgsource;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertiesFileMessageSourceTest {

    private static final String FILE = "message_source_test.properties";
    private final MessageSource source = new PropertiesFileMessageSource(FILE);

    @Test
    void codeWithoutVariantsResolvesToSingleMessage() {
        assertThat(source.getMessages("plain")).containsExactly("Plain message");
    }

    @Test
    void variantsAreCollectedInSuffixOrder() {
        assertThat(source.getMessages("greeting")).containsExactly("Hello", "Hi", "Hey");
    }

    @Test
    void variantCollectionStopsAtTheFirstGap() {
        // gapped.2 exists but gapped.1 does not, so only the base message is reachable
        assertThat(source.getMessages("gapped")).containsExactly("Base only");
    }

    @Test
    void unknownCodeYieldsEmptyList() {
        assertThat(source.getMessages("no.such.code")).isEmpty();
    }

    @Test
    void variantKeysAreExcludedFromReportedCodes() {
        assertThat(source.getMessageCodes())
                .contains("greeting")
                .doesNotContain("greeting.1", "greeting.2");
    }

    @Test
    void variantKeyLookedUpDirectlyStillResolves() {
        // Not reported as a code, but a direct lookup resolves it, as it did when every call
        // read straight from the Properties.
        assertThat(source.getMessages("greeting.1")).containsExactly("Hi");
    }

    @Test
    void returnedMessagesAreImmutable() {
        List<String> messages = source.getMessages("greeting");

        assertThatThrownBy(() -> messages.set(0, "tampered"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void repeatedLookupsReuseTheSameList() {
        assertThat(source.getMessages("greeting")).isSameAs(source.getMessages("greeting"));
    }

    @Test
    void missingFileFailsFast() {
        assertThatThrownBy(() -> new PropertiesFileMessageSource("no_such_file.properties"))
                .isInstanceOf(IllegalStateException.class);
    }
}
