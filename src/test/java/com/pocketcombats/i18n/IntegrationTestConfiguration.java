package com.pocketcombats.i18n;

import com.pocketcombats.i18n.spring.I18nConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
@ImportAutoConfiguration(I18nConfiguration.class)
public class IntegrationTestConfiguration {

    @Bean
    JsonMapper jsonMapper(List<? extends JacksonModule> jacksonModules) {
        return JsonMapper.builder().addModules(jacksonModules).build();
    }
}
