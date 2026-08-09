package com.pocketcombats.i18n.persistence;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

/**
 * Makes {@link LocalizedStringAttributeConverter} known to the persistence unit, so that its
 * {@code autoApply} takes effect.
 */
@AutoConfiguration
@ConditionalOnClass({
        EntityManagerFactory.class,
        EntityManagerFactoryBuilderCustomizer.class,
        PersistenceUnitPostProcessor.class
})
@Import(PersistenceAutoConfiguration.LocalizedStringConverterConfiguration.class)
public class PersistenceAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    static class LocalizedStringConverterConfiguration {

        @Bean
        EntityManagerFactoryBuilderCustomizer localizedStringConverterCustomizer() {
            return builder -> builder.addPersistenceUnitPostProcessors(
                    persistenceUnitInfo -> persistenceUnitInfo.addManagedClassName(
                            LocalizedStringAttributeConverter.class.getName())
            );
        }
    }
}
