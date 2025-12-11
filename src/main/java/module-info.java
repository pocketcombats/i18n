import org.jspecify.annotations.NullMarked;

@NullMarked
open module pocketcombats.i18n {
    requires org.jspecify;
    requires spring.core;
    requires spring.context;
    requires org.apache.commons.logging;
    requires com.ibm.icu;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.databind;
    requires org.apache.commons.lang3;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires static jakarta.persistence;

    exports com.pocketcombats.i18n;
    exports com.pocketcombats.i18n.msgsource;
    exports com.pocketcombats.i18n.formatter;
    exports com.pocketcombats.i18n.jackson;
    exports com.pocketcombats.i18n.spring;
    exports com.pocketcombats.i18n.persistence;
}
