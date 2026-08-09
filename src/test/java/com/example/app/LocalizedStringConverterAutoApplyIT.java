package com.example.app;

import com.pocketcombats.i18n.LocalizedString;
import com.pocketcombats.i18n.persistence.PersistenceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizedStringConverterAutoApplyIT {

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = Item.class)
    @EnableTransactionManagement
    static class ApplicationConfiguration {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ApplicationConfiguration.class)
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    HibernateJpaAutoConfiguration.class,
                    PersistenceAutoConfiguration.class
            ))
            .withPropertyValues("spring.jpa.hibernate.ddl-auto=create-drop");

    @Test
    void nullLocalizedStringRoundTripsAsNull() {
        runner.run(context -> {
            Long id = new TransactionTemplateHelper(context).persist(null);
            assertThat(new TransactionTemplateHelper(context).findTitle(id)).isNull();
        });
    }

    @Test
    void localizedStringRoundTripsWithoutAnyConvertAnnotation() {
        runner.run(context -> {
            LocalizedString title = LocalizedString.formatted(
                    "item.sword.name",
                    Map.of("QUALITY", LocalizedString.simple("quality.fine"))
            );

            Long id = new TransactionTemplateHelper(context).persist(title);

            LocalizedString loaded = new TransactionTemplateHelper(context).findTitle(id);
            assertThat(loaded).isEqualTo(title);
        });
    }
}
