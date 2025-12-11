package com.pocketcombats.i18n.persistence;

import com.pocketcombats.i18n.LocalizedString;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.Contract;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Converter(autoApply = true)
public class LocalizedStringAttributeConverter
        implements AttributeConverter<@Nullable LocalizedString, @Nullable String> {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .addModule(new SimpleModule().registerSubtypes(LocalizedString.class))
            .build();

    @Override
    @Contract("null -> null; !null -> !null")
    public @Nullable String convertToDatabaseColumn(@Nullable LocalizedString attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new IllegalStateException("Unable to serialize to JSON " + attribute, e);
        }
    }

    @Override
    @Contract("null -> null; !null -> !null")
    public @Nullable LocalizedString convertToEntityAttribute(@Nullable String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return MAPPER.readValue(dbData, LocalizedString.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Unable to deserialize " + dbData, e);
        }
    }
}
