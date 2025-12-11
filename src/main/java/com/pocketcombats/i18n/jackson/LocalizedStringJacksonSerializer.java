package com.pocketcombats.i18n.jackson;

import com.pocketcombats.i18n.I18nService;
import com.pocketcombats.i18n.LocalizedString;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

public class LocalizedStringJacksonSerializer extends StdSerializer<LocalizedString> {

    private final I18nService i18nService;

    public LocalizedStringJacksonSerializer(I18nService i18nService) {
        super(LocalizedString.class);
        this.i18nService = i18nService;
    }

    @Override
    public void serialize(
            LocalizedString value,
            JsonGenerator gen,
            SerializationContext provider
    ) throws JacksonException {
        String message = i18nService.getMessage(value);
        if (message == null) {
            throw new IllegalArgumentException("Unable to find localized message for " + value);
        }
        gen.writeString(message);
    }

    @Override
    public void serializeWithType(
            LocalizedString value,
            JsonGenerator gen,
            SerializationContext serializers,
            TypeSerializer typeSer
    ) throws JacksonException {
        String message = i18nService.getMessage(value);
        if (message == null) {
            throw new IllegalArgumentException("Unable to find localized message for " + value);
        }
        gen.writeString(message);
    }
}
