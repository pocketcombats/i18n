package com.pocketcombats.i18n.jackson;

import com.pocketcombats.i18n.LocalizedString;
import tools.jackson.databind.module.SimpleModule;

public class I18nModule extends SimpleModule {
    private static final String VERSION = "1.0";

    private I18nModule() {
        super(VERSION);
    }

    public static I18nModule forSerializer(LocalizedStringJacksonSerializer localizedStringJacksonSerializer) {
        I18nModule module = new I18nModule();
        module.addSerializer(LocalizedString.class, localizedStringJacksonSerializer);
        return module;
    }
}
