package com.pocketcombats.i18n.persistence;

import com.pocketcombats.i18n.LocalizedString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizedStringAttributeConverterTest {

    @Test
    void serializedValueCanBeDeserialized() {
        LocalizedStringAttributeConverter converter = new LocalizedStringAttributeConverter();

        var expected = LocalizedString.formatted(
                "common.skill.used",
                Map.of(
                        "NAME", LocalizedString.simple("galenus.name"),
                        "SKILL", LocalizedString.formatted(
                                "skill.avalanche.name",
                                Map.of("GENDER", "MALE")
                        ),
                        "DAMAGE", 123,
                        "SYSTEM", LocalizedString.joined("; ", List.of(LocalizedString.direct("Not enough mana")))
                )
        );
        String json = converter.convertToDatabaseColumn(expected);
        LocalizedString actual = converter.convertToEntityAttribute(json);

        assertThat(actual).isEqualTo(expected);
    }
}
