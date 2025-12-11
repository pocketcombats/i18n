package com.pocketcombats.i18n.jackson;

import com.pocketcombats.i18n.I18nService;
import com.pocketcombats.i18n.LocalizedString;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JacksonI18nModuleTest {

    @Test
    void localizedStringCustomSerialization() throws JacksonException {
        LocalizedString localizedString = LocalizedString.simple("custom.game.key");

        I18nService serviceMock = mock(I18nService.class);
        when(serviceMock.getMessage(localizedString)).thenReturn("Custom game key serialized correctly!");

        I18nModule i18nModule = I18nModule.forSerializer(new LocalizedStringJacksonSerializer(serviceMock));

        JsonMapper mapper = JsonMapper.builder().addModule(i18nModule).build();

        String jsonResult = mapper.writeValueAsString(new ComplexObject(666, localizedString));
        assertThat(jsonResult).isEqualTo("{\"id\":666,\"description\":\"Custom game key serialized correctly!\"}");
    }

    record ComplexObject(int id, LocalizedString description) {
    }
}
