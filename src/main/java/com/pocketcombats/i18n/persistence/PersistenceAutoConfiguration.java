package com.pocketcombats.i18n.persistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "jakarta.persistence.Converter")
//@EntityScan(basePackages = "com.pocketcombats.i18n.persistence")
public class PersistenceAutoConfiguration {
}
