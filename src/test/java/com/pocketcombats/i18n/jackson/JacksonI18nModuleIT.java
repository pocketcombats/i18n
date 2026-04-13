package com.pocketcombats.i18n.jackson;

import com.pocketcombats.i18n.I18nService;
import com.pocketcombats.i18n.IntegrationTestConfiguration;
import com.pocketcombats.i18n.LocalizedString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@TestPropertySource("classpath:application-test.properties")
class JacksonI18nModuleIT {

    @MockitoBean
    I18nService i18nService;

    @Autowired
    JsonMapper mapper;

    @Test
    void localizedStringCustomSerialization() throws JacksonException {
        LocalizedString localizedString = LocalizedString.simple("jackson.module.it");
        ComplexObject objectToSerialize = new ComplexObject(666, localizedString);

        when(i18nService.getMessage(localizedString)).thenReturn("Successful and correct serialization!");

        String jsonResult = mapper.writeValueAsString(objectToSerialize);
        assertThat(jsonResult).isEqualTo("{\"id\":666,\"description\":\"Successful and correct serialization!\"}");
    }

    record ComplexObject(int id, LocalizedString description) {
    }
}
